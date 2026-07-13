package com.feadev.eaids.analytics.prediction.impl;

import com.feadev.eaids.analytics.prediction.PotentialPredictionEngine;
import com.feadev.eaids.analytics.prediction.dto.PotentialPredictionResponse;
import com.feadev.eaids.analytics.prediction.event.PotentialPredictionGeneratedEvent;
import com.feadev.eaids.analytics.prediction.model.PotentialPrediction;
import com.feadev.eaids.analytics.prediction.model.enums.PredictionTier;
import com.feadev.eaids.analytics.prediction.repository.PotentialPredictionRepository;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.scoring.model.AthleteScore;
import com.feadev.eaids.scoring.repository.AthleteScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Rule-based prediction engine that uses cross-camp score deltas (short-term)
 * and projects future performance based on improvement rates.
 *
 * Classification tiers:
 * - ELITE_PROSPECT: top 5% improvement rate + top 20% absolute score
 * - HIGH_DEVELOPMENT: top 20% improvement rate
 * - SOLID_POTENTIAL: positive trajectory
 * - MONITOR: flat or declining trajectory
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class RuleBasedPredictionEngine implements PotentialPredictionEngine {

    private final PotentialPredictionRepository predictionRepository;
    private final AthleteScoreRepository athleteScoreRepository;
    private final CampEnrollmentRepository campEnrollmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    private static final int MIN_CAMPS_REQUIRED = 2;
    private static final BigDecimal ELITE_GROWTH_THRESHOLD = new BigDecimal("15.00"); // top 5% ~ >15% growth
    private static final BigDecimal ELITE_ABSOLUTE_THRESHOLD = new BigDecimal("80.00"); // top 20% ~ >80 score
    private static final BigDecimal HIGH_DEV_GROWTH_THRESHOLD = new BigDecimal("8.00"); // top 20% ~ >8% growth
    private static final BigDecimal POSITIVE_GROWTH_THRESHOLD = BigDecimal.ZERO;

    @Override
    @Transactional(readOnly = true)
    public PotentialPredictionResponse getPrediction(UUID athleteUserId) {
        // Check if we have a pre-calculated prediction
        Optional<PotentialPrediction> existing = predictionRepository
                .findTopByAthleteUserIdOrderByCalculatedAtDesc(athleteUserId);

        if (existing.isPresent()) {
            return mapToResponse(existing.get());
        }

        // Try to calculate on-the-fly
        return calculatePrediction(athleteUserId);
    }

    @Override
    @Transactional
    public List<PotentialPredictionResponse> generateBatchPredictions() {
        // Find all athletes who have scores in multiple camps
        List<UUID> athleteUserIds = findEligibleAthletes();

        List<PotentialPredictionResponse> results = new ArrayList<>();
        for (UUID athleteUserId : athleteUserIds) {
            PotentialPredictionResponse prediction = calculateAndPersist(athleteUserId);
            if (prediction != null) {
                results.add(prediction);
            }
        }

        log.info("Batch prediction generation complete: {} predictions generated", results.size());
        return results;
    }

    private PotentialPredictionResponse calculatePrediction(UUID athleteUserId) {
        // Get all enrollments for this athlete
        List<CampEnrollment> enrollments = campEnrollmentRepository.findByUserId(athleteUserId);

        if (enrollments.size() < MIN_CAMPS_REQUIRED) {
            return PotentialPredictionResponse.builder()
                    .athleteUserId(athleteUserId)
                    .message("Insufficient data: athlete needs at least " + MIN_CAMPS_REQUIRED +
                            " camp enrollments with completed scores. Currently has " + enrollments.size() + ".")
                    .build();
        }

        // Get scores for each enrollment, sorted by calculation date (oldest first)
        List<AthleteScore> scores = enrollments.stream()
                .map(enrollment -> athleteScoreRepository.findByEnrollmentId(enrollment.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(AthleteScore::getCalculatedAt))
                .collect(Collectors.toList());

        if (scores.size() < MIN_CAMPS_REQUIRED) {
            return PotentialPredictionResponse.builder()
                    .athleteUserId(athleteUserId)
                    .message("Insufficient data: athlete needs scores from at least " + MIN_CAMPS_REQUIRED +
                            " camps. Currently has scores from " + scores.size() + " camp(s).")
                    .build();
        }

        return buildPredictionResponse(athleteUserId, scores);
    }

    private PotentialPredictionResponse calculateAndPersist(UUID athleteUserId) {
        List<CampEnrollment> enrollments = campEnrollmentRepository.findByUserId(athleteUserId);

        if (enrollments.size() < MIN_CAMPS_REQUIRED) {
            return null;
        }

        List<AthleteScore> scores = enrollments.stream()
                .map(enrollment -> athleteScoreRepository.findByEnrollmentId(enrollment.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(AthleteScore::getCalculatedAt))
                .collect(Collectors.toList());

        if (scores.size() < MIN_CAMPS_REQUIRED) {
            return null;
        }

        PotentialPredictionResponse response = buildPredictionResponse(athleteUserId, scores);

        if (response.getPredictionTier() != null) {
            // Persist prediction
            PotentialPrediction prediction = PotentialPrediction.builder()
                    .athleteUserId(athleteUserId)
                    .predictionTier(response.getPredictionTier())
                    .overallGrowthRate(response.getOverallGrowthRate())
                    .physicalDelta(response.getPhysicalDelta())
                    .technicalDelta(response.getTechnicalDelta())
                    .tacticalDelta(response.getTacticalDelta())
                    .projectedScore1yr(response.getProjectedScore1yr())
                    .projectedScore2yr(response.getProjectedScore2yr())
                    .confidenceLevel(response.getConfidenceLevel())
                    .calculatedAt(LocalDateTime.now())
                    .build();

            predictionRepository.save(prediction);

            // Publish event
            eventPublisher.publishEvent(new PotentialPredictionGeneratedEvent(
                    athleteUserId, response.getPredictionTier()));
        }

        return response;
    }

    private PotentialPredictionResponse buildPredictionResponse(UUID athleteUserId, List<AthleteScore> scores) {
        // Calculate deltas between most recent and earliest scores
        AthleteScore earliest = scores.get(0);
        AthleteScore latest = scores.get(scores.size() - 1);

        BigDecimal physicalDelta = calculateDelta(earliest.getPhysicalScore(), latest.getPhysicalScore());
        BigDecimal technicalDelta = calculateDelta(earliest.getTechnicalScore(), latest.getTechnicalScore());
        BigDecimal tacticalDelta = calculateDelta(earliest.getTacticalScore(), latest.getTacticalScore());

        // Overall growth rate = average of individual deltas
        BigDecimal overallGrowth = physicalDelta.add(technicalDelta).add(tacticalDelta)
                .divide(new BigDecimal("3"), 2, RoundingMode.HALF_UP);

        // Latest total score for absolute performance check
        BigDecimal latestTotal = latest.getTotalWeightedScore() != null
                ? latest.getTotalWeightedScore() : BigDecimal.ZERO;

        // Classify tier
        PredictionTier tier = classifyTier(overallGrowth, latestTotal);

        // Project future scores based on growth rate
        BigDecimal projectedScore1yr = projectScore(latestTotal, overallGrowth, 1);
        BigDecimal projectedScore2yr = projectScore(latestTotal, overallGrowth, 2);

        // Confidence level based on number of data points (more camps = higher confidence)
        BigDecimal confidence = calculateConfidence(scores.size());

        return PotentialPredictionResponse.builder()
                .athleteUserId(athleteUserId)
                .predictionTier(tier)
                .overallGrowthRate(overallGrowth)
                .physicalDelta(physicalDelta)
                .technicalDelta(technicalDelta)
                .tacticalDelta(tacticalDelta)
                .projectedScore1yr(projectedScore1yr)
                .projectedScore2yr(projectedScore2yr)
                .confidenceLevel(confidence)
                .calculatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Calculate percentage change between two scores.
     */
    private BigDecimal calculateDelta(BigDecimal earlier, BigDecimal later) {
        if (earlier == null || later == null) {
            return BigDecimal.ZERO;
        }
        if (earlier.compareTo(BigDecimal.ZERO) == 0) {
            return later.compareTo(BigDecimal.ZERO) > 0 ? new BigDecimal("100.00") : BigDecimal.ZERO;
        }
        return later.subtract(earlier)
                .divide(earlier, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Classify athlete into a prediction tier based on growth rate and absolute performance.
     */
    private PredictionTier classifyTier(BigDecimal growthRate, BigDecimal absoluteScore) {
        if (growthRate.compareTo(ELITE_GROWTH_THRESHOLD) >= 0
                && absoluteScore.compareTo(ELITE_ABSOLUTE_THRESHOLD) >= 0) {
            return PredictionTier.ELITE_PROSPECT;
        }
        if (growthRate.compareTo(HIGH_DEV_GROWTH_THRESHOLD) >= 0) {
            return PredictionTier.HIGH_DEVELOPMENT;
        }
        if (growthRate.compareTo(POSITIVE_GROWTH_THRESHOLD) > 0) {
            return PredictionTier.SOLID_POTENTIAL;
        }
        return PredictionTier.MONITOR;
    }

    /**
     * Project future score based on current growth rate.
     * Uses diminishing returns model (growth rate decreases by 30% each year).
     */
    private BigDecimal projectScore(BigDecimal currentScore, BigDecimal growthRate, int years) {
        BigDecimal projected = currentScore;
        BigDecimal annualRate = growthRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        BigDecimal decay = new BigDecimal("0.70"); // 30% decay per year

        for (int i = 0; i < years; i++) {
            BigDecimal effectiveRate = annualRate.multiply(decay.pow(i));
            projected = projected.add(projected.multiply(effectiveRate));
        }

        // Cap at 99.99
        BigDecimal cap = new BigDecimal("99.99");
        return projected.min(cap).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate confidence level based on number of camp scores available.
     * 2 camps = 50%, 3 camps = 70%, 4 camps = 85%, 5+ camps = 95%
     */
    private BigDecimal calculateConfidence(int numberOfCamps) {
        return switch (numberOfCamps) {
            case 2 -> new BigDecimal("50.00");
            case 3 -> new BigDecimal("70.00");
            case 4 -> new BigDecimal("85.00");
            default -> new BigDecimal("95.00");
        };
    }

    /**
     * Find all athlete user IDs that have enrollments in 2+ camps with scores.
     */
    private List<UUID> findEligibleAthletes() {
        // Get all enrollments grouped by user, filter for those with 2+ enrollments
        List<CampEnrollment> allEnrollments = campEnrollmentRepository.findAll();

        Map<UUID, List<CampEnrollment>> byUser = allEnrollments.stream()
                .collect(Collectors.groupingBy(e -> e.getUser().getId()));

        return byUser.entrySet().stream()
                .filter(entry -> entry.getValue().size() >= MIN_CAMPS_REQUIRED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    private PotentialPredictionResponse mapToResponse(PotentialPrediction prediction) {
        return PotentialPredictionResponse.builder()
                .id(prediction.getId())
                .athleteUserId(prediction.getAthleteUserId())
                .predictionTier(prediction.getPredictionTier())
                .overallGrowthRate(prediction.getOverallGrowthRate())
                .physicalDelta(prediction.getPhysicalDelta())
                .technicalDelta(prediction.getTechnicalDelta())
                .tacticalDelta(prediction.getTacticalDelta())
                .projectedScore1yr(prediction.getProjectedScore1yr())
                .projectedScore2yr(prediction.getProjectedScore2yr())
                .confidenceLevel(prediction.getConfidenceLevel())
                .calculatedAt(prediction.getCalculatedAt())
                .build();
    }
}
