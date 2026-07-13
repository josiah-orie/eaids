package com.feadev.eaids.ranking.service.impl;

import com.feadev.eaids.ranking.dto.response.RankingResponse;
import com.feadev.eaids.ranking.event.RankingsGeneratedEvent;
import com.feadev.eaids.ranking.model.AthleteRanking;
import com.feadev.eaids.ranking.model.enums.RankingType;
import com.feadev.eaids.ranking.repository.AthleteRankingRepository;
import com.feadev.eaids.ranking.service.RankingService;
import com.feadev.eaids.registrations.model.AthleteProfile;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.scoring.event.ScoresCalculatedEvent;
import com.feadev.eaids.scoring.model.AthleteScore;
import com.feadev.eaids.scoring.repository.AthleteScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
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
@Transactional
public class RankingServiceImpl implements RankingService {

    private final AthleteRankingRepository rankingRepository;
    private final AthleteScoreRepository athleteScoreRepository;
    private final AthleteProfileRepository athleteProfileRepository;
    private final ApplicationEventPublisher eventPublisher;

    @EventListener
    public void onScoresCalculated(ScoresCalculatedEvent event) {
        log.info("Scores calculated event received for camp {}. Auto-generating rankings.", event.getCampId());
        generateRankings(event.getCampId());
    }

    @Override
    public List<RankingResponse> generateRankings(UUID campId) {
        // Clear existing rankings for this camp
        rankingRepository.deleteByCampId(campId);

        List<AthleteScore> scores = athleteScoreRepository.findByCampIdOrderByTotalWeightedScoreDesc(campId);
        if (scores.isEmpty()) {
            log.warn("No scores found for camp {}. Cannot generate rankings.", campId);
            return Collections.emptyList();
        }

        LocalDateTime now = LocalDateTime.now();
        List<AthleteRanking> allRankings = new ArrayList<>();

        // OVERALL rankings
        allRankings.addAll(createRankingsForGroup(scores, campId, RankingType.OVERALL, null, now));

        // POSITION rankings
        Map<String, List<AthleteScore>> byPosition = scores.stream()
                .collect(Collectors.groupingBy(s -> s.getPosition().name()));
        for (Map.Entry<String, List<AthleteScore>> entry : byPosition.entrySet()) {
            allRankings.addAll(createRankingsForGroup(entry.getValue(), campId, RankingType.POSITION, entry.getKey(), now));
        }

        // GENDER, COUNTRY, STATE rankings - need athlete profiles
        Map<UUID, AthleteProfile> profileCache = new HashMap<>();
        for (AthleteScore score : scores) {
            UUID userId = score.getEnrollment().getUser().getId();
            if (!profileCache.containsKey(userId)) {
                athleteProfileRepository.findByUser_Id(userId).ifPresent(p -> profileCache.put(userId, p));
            }
        }

        // GENDER rankings
        Map<String, List<AthleteScore>> byGender = scores.stream()
                .filter(s -> {
                    AthleteProfile profile = profileCache.get(s.getEnrollment().getUser().getId());
                    return profile != null && profile.getGender() != null;
                })
                .collect(Collectors.groupingBy(s -> {
                    AthleteProfile profile = profileCache.get(s.getEnrollment().getUser().getId());
                    return profile.getGender().name();
                }));
        for (Map.Entry<String, List<AthleteScore>> entry : byGender.entrySet()) {
            allRankings.addAll(createRankingsForGroup(entry.getValue(), campId, RankingType.GENDER, entry.getKey(), now));
        }

        // COUNTRY rankings
        Map<String, List<AthleteScore>> byCountry = scores.stream()
                .filter(s -> {
                    AthleteProfile profile = profileCache.get(s.getEnrollment().getUser().getId());
                    return profile != null && profile.getCountryOfResidence() != null;
                })
                .collect(Collectors.groupingBy(s -> {
                    AthleteProfile profile = profileCache.get(s.getEnrollment().getUser().getId());
                    return profile.getCountryOfResidence();
                }));
        for (Map.Entry<String, List<AthleteScore>> entry : byCountry.entrySet()) {
            allRankings.addAll(createRankingsForGroup(entry.getValue(), campId, RankingType.COUNTRY, entry.getKey(), now));
        }

        // STATE rankings
        Map<String, List<AthleteScore>> byState = scores.stream()
                .filter(s -> {
                    AthleteProfile profile = profileCache.get(s.getEnrollment().getUser().getId());
                    return profile != null && profile.getStateOfResidence() != null;
                })
                .collect(Collectors.groupingBy(s -> {
                    AthleteProfile profile = profileCache.get(s.getEnrollment().getUser().getId());
                    return profile.getStateOfResidence();
                }));
        for (Map.Entry<String, List<AthleteScore>> entry : byState.entrySet()) {
            allRankings.addAll(createRankingsForGroup(entry.getValue(), campId, RankingType.STATE, entry.getKey(), now));
        }

        List<AthleteRanking> saved = rankingRepository.saveAll(allRankings);
        eventPublisher.publishEvent(new RankingsGeneratedEvent(campId));
        log.info("Rankings generated for camp {}. Total ranking entries: {}", campId, saved.size());

        return saved.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankingResponse> getRankingsByType(UUID campId, RankingType type) {
        return rankingRepository.findByCampIdAndRankingType(campId, type).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankingResponse> getRankingsByTypeAndFilter(UUID campId, RankingType type, String filterValue) {
        return rankingRepository.findByCampIdAndRankingTypeAndFilterValue(campId, type, filterValue).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RankingResponse> getRankingsByEnrollment(UUID enrollmentId) {
        return rankingRepository.findByEnrollmentId(enrollmentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private List<AthleteRanking> createRankingsForGroup(
            List<AthleteScore> scores, UUID campId, RankingType type, String filterValue, LocalDateTime now) {

        // Sort by total weighted score descending
        List<AthleteScore> sorted = scores.stream()
                .sorted(Comparator.comparing(AthleteScore::getTotalWeightedScore).reversed())
                .collect(Collectors.toList());

        int totalInCategory = sorted.size();
        List<AthleteRanking> rankings = new ArrayList<>();

        for (int i = 0; i < sorted.size(); i++) {
            AthleteScore score = sorted.get(i);
            int rankValue = i + 1;

            BigDecimal percentile = totalInCategory > 1
                    ? BigDecimal.valueOf(totalInCategory - rankValue)
                        .divide(BigDecimal.valueOf(totalInCategory), 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100.00"))
                        .setScale(2, RoundingMode.HALF_UP)
                    : new BigDecimal("100.00");

            AthleteRanking ranking = AthleteRanking.builder()
                    .enrollment(score.getEnrollment())
                    .campId(campId)
                    .rankingType(type)
                    .rankValue(rankValue)
                    .totalInCategory(totalInCategory)
                    .percentile(percentile)
                    .score(score.getTotalWeightedScore())
                    .filterValue(filterValue)
                    .calculatedAt(now)
                    .build();

            rankings.add(ranking);
        }

        return rankings;
    }

    private RankingResponse mapToResponse(AthleteRanking ranking) {
        String athleteName = "Unknown";
        if (ranking.getEnrollment() != null && ranking.getEnrollment().getUser() != null) {
            athleteName = athleteProfileRepository.findByUser_Id(ranking.getEnrollment().getUser().getId())
                    .map(profile -> profile.getFullName())
                    .orElse("Unknown");
        }

        return RankingResponse.builder()
                .id(ranking.getId())
                .enrollmentId(ranking.getEnrollment().getId())
                .athleteName(athleteName)
                .campId(ranking.getCampId())
                .rankingType(ranking.getRankingType())
                .rankValue(ranking.getRankValue())
                .totalInCategory(ranking.getTotalInCategory())
                .percentile(ranking.getPercentile())
                .score(ranking.getScore())
                .filterValue(ranking.getFilterValue())
                .calculatedAt(ranking.getCalculatedAt())
                .build();
    }
}
