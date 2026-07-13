package com.feadev.eaids.analytics.recommendation.impl;

import com.feadev.eaids.analytics.recommendation.PositionRecommendationEngine;
import com.feadev.eaids.analytics.recommendation.dto.PositionFitScore;
import com.feadev.eaids.analytics.recommendation.dto.PositionRecommendationResponse;
import com.feadev.eaids.analytics.recommendation.event.PositionRecommendationGeneratedEvent;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.positionprofile.model.PositionProfile;
import com.feadev.eaids.positionprofile.model.PositionWeightTemplate;
import com.feadev.eaids.positionprofile.repository.PositionProfileRepository;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
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
 * Rule-based position recommendation engine.
 * Calculates a "fit score" for each active position profile by comparing
 * the athlete's physical/technical/tactical scores against the position's weight template.
 * Higher weight on a category means that category matters more for that position.
 */
@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class RuleBasedRecommendationEngine implements PositionRecommendationEngine {

    private final AthleteScoreRepository athleteScoreRepository;
    private final PositionProfileRepository positionProfileRepository;
    private final CampEnrollmentRepository campEnrollmentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public PositionRecommendationResponse recommend(UUID enrollmentId) {
        // Get athlete score for this enrollment
        AthleteScore athleteScore = athleteScoreRepository.findByEnrollmentId(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No scores found for enrollment: " + enrollmentId + ". Scores must be calculated first."));

        CampEnrollment enrollment = campEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + enrollmentId));

        User athlete = enrollment.getUser();

        // Get all active position profiles
        List<PositionProfile> activeProfiles = positionProfileRepository.findByIsActiveTrue();

        if (activeProfiles.isEmpty()) {
            throw new ResourceNotFoundException("No active position profiles configured in the system");
        }

        // Calculate fit score for each position profile
        List<PositionFitScore> allScores = activeProfiles.stream()
                .map(profile -> calculateFitScore(athleteScore, profile))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(PositionFitScore::getFitScore).reversed())
                .collect(Collectors.toList());

        if (allScores.isEmpty()) {
            throw new ResourceNotFoundException("Unable to calculate fit scores. Position profiles may be missing weight templates.");
        }

        // Best = highest fit score
        PositionFitScore best = allScores.get(0);

        // Secondary = second highest (if exists)
        PositionFitScore secondary = allScores.size() > 1 ? allScores.get(1) : null;

        // Potential = position where growth trajectory projects highest improvement
        // For now, use the position with highest technical score alignment as "potential"
        // (since technical skill is most improvable through training)
        PositionFitScore potential = calculatePotentialPosition(athleteScore, activeProfiles, best, secondary);

        PositionRecommendationResponse response = PositionRecommendationResponse.builder()
                .enrollmentId(enrollmentId)
                .athleteUserId(athlete.getId())
                .athleteName(athlete.getEmail()) // email as fallback identifier
                .bestPosition(best.getPosition())
                .bestPositionFitScore(best.getFitScore())
                .secondaryPosition(secondary != null ? secondary.getPosition() : null)
                .secondaryFitScore(secondary != null ? secondary.getFitScore() : null)
                .potentialPosition(potential != null ? potential.getPosition() : null)
                .potentialFitScore(potential != null ? potential.getFitScore() : null)
                .allPositionScores(allScores)
                .generatedAt(LocalDateTime.now())
                .build();

        // Publish event
        eventPublisher.publishEvent(new PositionRecommendationGeneratedEvent(
                enrollmentId, athlete.getId(), best.getPosition()));

        log.info("Position recommendation generated: enrollmentId={}, bestPosition={}, fitScore={}",
                enrollmentId, best.getPosition(), best.getFitScore());

        return response;
    }

    /**
     * Calculate the fit score for an athlete against a specific position profile.
     * The fit score is a weighted composite: for each category (physical, technical, tactical, etc.),
     * multiply the athlete's normalized score by the position's weight for that category.
     */
    private PositionFitScore calculateFitScore(AthleteScore athleteScore, PositionProfile profile) {
        PositionWeightTemplate weights = profile.getWeightTemplate();
        if (weights == null) {
            log.warn("Position profile '{}' ({}) has no weight template, skipping",
                    profile.getName(), profile.getPosition());
            return null;
        }

        // Normalize athlete scores (they are 0-100 scale based on schema)
        BigDecimal physicalNorm = normalize(athleteScore.getPhysicalScore());
        BigDecimal technicalNorm = normalize(athleteScore.getTechnicalScore());
        BigDecimal tacticalNorm = normalize(athleteScore.getTacticalScore());
        BigDecimal attendanceNorm = normalize(athleteScore.getAttendanceScore());
        BigDecimal disciplineNorm = normalize(athleteScore.getDisciplineScore());
        BigDecimal coachReviewNorm = normalize(athleteScore.getCoachReviewScore());
        BigDecimal matchStatsNorm = normalize(athleteScore.getMatchStatsScore());

        // Calculate weighted sum using position weight template
        // Weights are percentages (sum to 100), so divide by 100 for the final score
        BigDecimal totalWeight = weights.getPhysicalWeight()
                .add(weights.getTechnicalWeight())
                .add(weights.getTacticalWeight())
                .add(weights.getAttendanceWeight())
                .add(weights.getDisciplineWeight())
                .add(weights.getCoachReviewWeight())
                .add(weights.getMatchStatsWeight());

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        BigDecimal fitScore = physicalNorm.multiply(weights.getPhysicalWeight())
                .add(technicalNorm.multiply(weights.getTechnicalWeight()))
                .add(tacticalNorm.multiply(weights.getTacticalWeight()))
                .add(attendanceNorm.multiply(weights.getAttendanceWeight()))
                .add(disciplineNorm.multiply(weights.getDisciplineWeight()))
                .add(coachReviewNorm.multiply(weights.getCoachReviewWeight()))
                .add(matchStatsNorm.multiply(weights.getMatchStatsWeight()))
                .divide(totalWeight, 2, RoundingMode.HALF_UP);

        return PositionFitScore.builder()
                .position(profile.getPosition())
                .fitScore(fitScore)
                .profileName(profile.getName())
                .build();
    }

    /**
     * Calculate the "potential" position — the position where the athlete could
     * excel with development. This considers positions where the technical component
     * weight is high (since technical skills are most trainable), and where the
     * athlete's raw physical attributes already provide a strong base.
     */
    private PositionFitScore calculatePotentialPosition(AthleteScore athleteScore,
                                                         List<PositionProfile> profiles,
                                                         PositionFitScore best,
                                                         PositionFitScore secondary) {
        // Calculate a "potential fit" that emphasizes physical attributes
        // (natural gifts that are harder to train) combined with technical trainability
        return profiles.stream()
                .map(profile -> calculatePotentialFitScore(athleteScore, profile))
                .filter(Objects::nonNull)
                .filter(pfs -> !pfs.getPosition().equals(best.getPosition()))
                .filter(pfs -> secondary == null || !pfs.getPosition().equals(secondary.getPosition()))
                .max(Comparator.comparing(PositionFitScore::getFitScore))
                .orElse(null);
    }

    /**
     * Potential fit emphasizes physical (60%) + tactical (40%) potential,
     * as technical skills can be developed through coaching.
     */
    private PositionFitScore calculatePotentialFitScore(AthleteScore athleteScore, PositionProfile profile) {
        PositionWeightTemplate weights = profile.getWeightTemplate();
        if (weights == null) {
            return null;
        }

        BigDecimal physicalNorm = normalize(athleteScore.getPhysicalScore());
        BigDecimal tacticalNorm = normalize(athleteScore.getTacticalScore());

        // Weight physical attributes more heavily for potential (natural gifts)
        BigDecimal potentialScore = physicalNorm.multiply(new BigDecimal("0.60"))
                .add(tacticalNorm.multiply(new BigDecimal("0.40")));

        return PositionFitScore.builder()
                .position(profile.getPosition())
                .fitScore(potentialScore.setScale(2, RoundingMode.HALF_UP))
                .profileName(profile.getName())
                .build();
    }

    private BigDecimal normalize(BigDecimal score) {
        if (score == null) {
            return BigDecimal.ZERO;
        }
        // Scores are already on 0-100 scale, normalize to 0-1 for calculation
        return score.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
    }
}
