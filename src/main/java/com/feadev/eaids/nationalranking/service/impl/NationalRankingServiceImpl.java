package com.feadev.eaids.nationalranking.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.nationalranking.dto.request.CreateRankingConfigRequest;
import com.feadev.eaids.nationalranking.dto.response.NationalRankingResponse;
import com.feadev.eaids.nationalranking.dto.response.RankingConfigResponse;
import com.feadev.eaids.nationalranking.event.NationalRankingsGeneratedEvent;
import com.feadev.eaids.nationalranking.model.NationalRanking;
import com.feadev.eaids.nationalranking.model.NationalRankingConfig;
import com.feadev.eaids.nationalranking.model.enums.RankingScope;
import com.feadev.eaids.nationalranking.repository.NationalRankingConfigRepository;
import com.feadev.eaids.nationalranking.repository.NationalRankingRepository;
import com.feadev.eaids.nationalranking.service.NationalRankingService;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.scoring.model.AthleteScore;
import com.feadev.eaids.scoring.repository.AthleteScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NationalRankingServiceImpl implements NationalRankingService {

    private final NationalRankingConfigRepository configRepository;
    private final NationalRankingRepository rankingRepository;
    private final CampEnrollmentRepository campEnrollmentRepository;
    private final AthleteScoreRepository athleteScoreRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public RankingConfigResponse createConfig(CreateRankingConfigRequest request) {
        String weightFormulaJson;
        String ageCategoriesJson;
        String positionsJson;

        try {
            weightFormulaJson = objectMapper.writeValueAsString(request.getWeightFormula());
            ageCategoriesJson = request.getAgeCategories() != null
                    ? objectMapper.writeValueAsString(request.getAgeCategories()) : null;
            positionsJson = request.getPositions() != null
                    ? objectMapper.writeValueAsString(request.getPositions()) : null;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JSON in request: " + e.getMessage());
        }

        NationalRankingConfig config = NationalRankingConfig.builder()
                .name(request.getName())
                .description(request.getDescription())
                .weightFormula(weightFormulaJson)
                .minCampsRequired(request.getMinCampsRequired())
                .ageCategories(ageCategoriesJson)
                .positions(positionsJson)
                .active(true)
                .build();

        NationalRankingConfig saved = configRepository.save(config);
        log.info("National ranking config created: id={}, name={}", saved.getId(), saved.getName());
        return mapConfigToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankingConfigResponse> getAllConfigs() {
        return configRepository.findAll().stream()
                .map(this::mapConfigToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public int generateRankings(UUID configId) {
        NationalRankingConfig config = configRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Ranking config not found: " + configId));

        // Parse weight formula
        List<CreateRankingConfigRequest.RecencyWeight> weights;
        try {
            weights = objectMapper.readValue(config.getWeightFormula(),
                    new TypeReference<List<CreateRankingConfigRequest.RecencyWeight>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Invalid weight formula in config: " + e.getMessage());
        }

        // Delete previous rankings for this config
        rankingRepository.deleteByConfigId(configId);

        // Get all athletes with their scores across camps
        List<CampEnrollment> allEnrollments = campEnrollmentRepository.findAll();
        Map<UUID, List<CampEnrollment>> byUser = allEnrollments.stream()
                .collect(Collectors.groupingBy(e -> e.getUser().getId()));

        // Calculate composite scores for eligible athletes
        List<AthleteComposite> composites = new ArrayList<>();

        for (Map.Entry<UUID, List<CampEnrollment>> entry : byUser.entrySet()) {
            UUID athleteUserId = entry.getKey();
            List<CampEnrollment> enrollments = entry.getValue();

            if (enrollments.size() < config.getMinCampsRequired()) {
                continue;
            }

            // Get scores sorted by most recent first
            List<AthleteScore> scores = enrollments.stream()
                    .map(e -> athleteScoreRepository.findByEnrollmentId(e.getId()))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .sorted(Comparator.comparing(AthleteScore::getCalculatedAt).reversed())
                    .collect(Collectors.toList());

            if (scores.size() < config.getMinCampsRequired()) {
                continue;
            }

            BigDecimal compositeScore = calculateCompositeScore(scores, weights);
            AthleteScore latestScore = scores.get(0);

            composites.add(new AthleteComposite(
                    athleteUserId, compositeScore, latestScore.getPosition(),
                    null, null)); // age/state will be resolved later if needed
        }

        if (composites.isEmpty()) {
            log.info("No eligible athletes for ranking config: {}", configId);
            return 0;
        }

        // Sort by composite score descending and assign national ranks
        composites.sort(Comparator.comparing(AthleteComposite::compositeScore).reversed());

        int totalAthletes = composites.size();
        List<NationalRanking> rankings = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < composites.size(); i++) {
            AthleteComposite ac = composites.get(i);
            int rank = i + 1;
            BigDecimal percentile = BigDecimal.valueOf((totalAthletes - rank + 1.0) / totalAthletes * 100)
                    .setScale(2, RoundingMode.HALF_UP);

            rankings.add(NationalRanking.builder()
                    .athleteUserId(ac.athleteUserId())
                    .configId(configId)
                    .compositeScore(ac.compositeScore())
                    .rankValue(rank)
                    .totalAthletes(totalAthletes)
                    .percentile(percentile)
                    .rankingScope(RankingScope.NATIONAL)
                    .position(ac.position())
                    .generatedAt(now)
                    .build());
        }

        rankingRepository.saveAll(rankings);

        // Publish event
        eventPublisher.publishEvent(new NationalRankingsGeneratedEvent(configId, rankings.size()));

        log.info("National rankings generated: configId={}, totalRankings={}", configId, rankings.size());
        return rankings.size();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NationalRankingResponse> getTopRankings(RankingScope scope, String scopeValue, Pageable pageable) {
        return rankingRepository.findByScope(scope, scopeValue, pageable)
                .map(this::mapRankingToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NationalRankingResponse> getAthleteRankings(UUID athleteUserId) {
        return rankingRepository.findByAthleteUserIdOrderByGeneratedAtDesc(athleteUserId).stream()
                .map(this::mapRankingToResponse)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateCompositeScore(List<AthleteScore> scores,
                                                List<CreateRankingConfigRequest.RecencyWeight> weights) {
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (int i = 0; i < scores.size() && i < weights.size(); i++) {
            AthleteScore score = scores.get(i);
            BigDecimal weight = BigDecimal.valueOf(weights.get(i).getWeight());

            if (score.getTotalWeightedScore() != null) {
                totalScore = totalScore.add(score.getTotalWeightedScore().multiply(weight));
                totalWeight = totalWeight.add(weight);
            }
        }

        // If we have more scores than configured weights, use remaining with zero weight (ignored)
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalScore.divide(totalWeight, 2, RoundingMode.HALF_UP);
    }

    private RankingConfigResponse mapConfigToResponse(NationalRankingConfig config) {
        return RankingConfigResponse.builder()
                .id(config.getId())
                .name(config.getName())
                .description(config.getDescription())
                .weightFormula(config.getWeightFormula())
                .minCampsRequired(config.getMinCampsRequired())
                .ageCategories(config.getAgeCategories())
                .positions(config.getPositions())
                .active(config.isActive())
                .createdDate(config.getCreatedDate())
                .build();
    }

    private NationalRankingResponse mapRankingToResponse(NationalRanking ranking) {
        return NationalRankingResponse.builder()
                .id(ranking.getId())
                .athleteUserId(ranking.getAthleteUserId())
                .compositeScore(ranking.getCompositeScore())
                .rankValue(ranking.getRankValue())
                .totalAthletes(ranking.getTotalAthletes())
                .percentile(ranking.getPercentile())
                .rankingScope(ranking.getRankingScope())
                .scopeValue(ranking.getScopeValue())
                .position(ranking.getPosition())
                .ageCategory(ranking.getAgeCategory())
                .generatedAt(ranking.getGeneratedAt())
                .build();
    }

    private record AthleteComposite(UUID athleteUserId, BigDecimal compositeScore,
                                     com.feadev.eaids.technical.model.enums.FootballPosition position,
                                     String state, String ageCategory) {}
}
