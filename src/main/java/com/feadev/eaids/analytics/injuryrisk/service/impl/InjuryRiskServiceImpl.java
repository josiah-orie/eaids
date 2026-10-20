package com.feadev.eaids.analytics.injuryrisk.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feadev.eaids.analytics.injuryrisk.dto.HolisticRiskResponse;
import com.feadev.eaids.analytics.injuryrisk.dto.InjuryRiskResponse;
import com.feadev.eaids.analytics.injuryrisk.enrichment.MedicalRiskEnricher;
import com.feadev.eaids.analytics.injuryrisk.enrichment.MedicalRiskSnapshot;
import com.feadev.eaids.analytics.injuryrisk.enrichment.OverallRiskLevelResolver;
import com.feadev.eaids.analytics.injuryrisk.event.InjuryRiskAssessedEvent;
import com.feadev.eaids.analytics.injuryrisk.model.InjuryRiskAssessment;
import com.feadev.eaids.analytics.injuryrisk.model.enums.RiskLevel;
import com.feadev.eaids.analytics.injuryrisk.repository.InjuryRiskRepository;
import com.feadev.eaids.analytics.injuryrisk.service.InjuryRiskService;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.scoring.model.AthleteScore;
import com.feadev.eaids.scoring.repository.AthleteScoreRepository;
import com.feadev.eaids.trainingload.model.TrainingLoadRecord;
import com.feadev.eaids.trainingload.model.enums.TrainingIntensity;
import com.feadev.eaids.trainingload.repository.TrainingLoadRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InjuryRiskServiceImpl implements InjuryRiskService {

    private final InjuryRiskRepository injuryRiskRepository;
    private final TrainingLoadRepository trainingLoadRepository;
    private final CampEnrollmentRepository campEnrollmentRepository;
    private final AthleteScoreRepository athleteScoreRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;
    private final MedicalRiskEnricher medicalRiskEnricher;
    private final OverallRiskLevelResolver overallRiskLevelResolver;

    private static final BigDecimal ACWR_DANGER_THRESHOLD = new BigDecimal("1.50");
    private static final BigDecimal PERFORMANCE_DROP_THRESHOLD = new BigDecimal("15.00");
    private static final int CONSECUTIVE_HIGH_DAYS_THRESHOLD = 3;

    @Override
    @Transactional
    public InjuryRiskResponse assessEnrollment(UUID enrollmentId) {
        CampEnrollment enrollment = campEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + enrollmentId));

        UUID athleteUserId = enrollment.getUser().getId();

        // Calculate training risk factors
        BigDecimal loadFactor = calculateLoadFactor(enrollmentId);
        BigDecimal attendanceFactor = calculateAttendanceFactor(enrollmentId);
        BigDecimal performanceFactor = calculatePerformanceFactor(athleteUserId, enrollmentId);

        // Composite training risk score (0-100)
        int trainingRiskScore = calculateCompositeRiskScore(loadFactor, attendanceFactor, performanceFactor);
        RiskLevel trainingRiskLevel = classifyRiskLevel(trainingRiskScore);

        // Generate training recommendations
        List<String> recommendations = generateRecommendations(loadFactor, attendanceFactor, performanceFactor);

        String recommendationsJson;
        try {
            recommendationsJson = objectMapper.writeValueAsString(recommendations);
        } catch (JsonProcessingException e) {
            recommendationsJson = "[]";
        }

        InjuryRiskAssessment assessment = InjuryRiskAssessment.builder()
                .athleteUserId(athleteUserId)
                .enrollmentId(enrollmentId)
                .riskScore(trainingRiskScore)
                .riskLevel(trainingRiskLevel)
                .loadFactorScore(loadFactor)
                .attendanceFactorScore(attendanceFactor)
                .performanceFactorScore(performanceFactor)
                .recommendations(recommendationsJson)
                .assessedAt(LocalDateTime.now())
                .build();

        InjuryRiskAssessment saved = injuryRiskRepository.save(assessment);

        // Enrich with medical history data (computed on-the-fly, not persisted)
        MedicalRiskSnapshot medicalSnapshot = medicalRiskEnricher.enrich(athleteUserId);
        RiskLevel overallRiskLevel = overallRiskLevelResolver.resolve(trainingRiskLevel, medicalSnapshot);

        // Publish event
        eventPublisher.publishEvent(new InjuryRiskAssessedEvent(
                enrollmentId, athleteUserId, trainingRiskLevel, trainingRiskScore));

        log.info("Injury risk assessed: enrollmentId={}, trainingRiskScore={}, trainingRiskLevel={}, " +
                 "medicalRiskScore={}, medicalRiskLevel={}, overallRiskLevel={}",
                enrollmentId, trainingRiskScore, trainingRiskLevel,
                medicalSnapshot.medicalRiskScore(), medicalSnapshot.medicalRiskLevel(), overallRiskLevel);

        return mapToResponse(saved, recommendations, medicalSnapshot, overallRiskLevel);
    }

    @Override
    @Transactional
    public List<InjuryRiskResponse> batchAssessCamp(UUID campId) {
        List<CampEnrollment> enrollments = campEnrollmentRepository.findByCampId(campId);

        return enrollments.stream()
                .map(enrollment -> {
                    try {
                        return assessEnrollment(enrollment.getId());
                    } catch (Exception e) {
                        log.warn("Failed to assess enrollment {}: {}", enrollment.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HolisticRiskResponse getAthleteFullRiskProfile(UUID athleteUserId) {
        // Fetch the most recent training assessment if one exists
        List<InjuryRiskAssessment> assessments =
                injuryRiskRepository.findByAthleteUserIdOrderByAssessedAtDesc(athleteUserId);
        InjuryRiskAssessment latest = assessments.isEmpty() ? null : assessments.get(0);

        int trainingRiskScore       = latest != null ? latest.getRiskScore()             : 0;
        RiskLevel trainingRiskLevel = latest != null ? latest.getRiskLevel()             : RiskLevel.LOW;
        BigDecimal loadFactor       = latest != null ? latest.getLoadFactorScore()       : BigDecimal.ZERO;
        BigDecimal attendanceFactor = latest != null ? latest.getAttendanceFactorScore() : BigDecimal.ZERO;
        BigDecimal performanceFactor= latest != null ? latest.getPerformanceFactorScore(): BigDecimal.ZERO;
        LocalDateTime assessedAt    = latest != null ? latest.getAssessedAt()            : LocalDateTime.now();

        // Deserialise recommendations JSON from the stored assessment
        List<String> recommendations = List.of();
        if (latest != null && latest.getRecommendations() != null) {
            try {
                recommendations = objectMapper.readValue(
                        latest.getRecommendations(), new TypeReference<List<String>>() {});
            } catch (JsonProcessingException e) {
                log.warn("Failed to parse recommendations JSON for athlete {}", athleteUserId);
            }
        }

        MedicalRiskSnapshot medicalSnapshot = medicalRiskEnricher.enrich(athleteUserId);
        RiskLevel overallRiskLevel = overallRiskLevelResolver.resolve(trainingRiskLevel, medicalSnapshot);

        return HolisticRiskResponse.builder()
                .athleteUserId(athleteUserId)
                .trainingRiskScore(trainingRiskScore)
                .trainingRiskLevel(trainingRiskLevel)
                .loadFactorScore(loadFactor)
                .attendanceFactorScore(attendanceFactor)
                .performanceFactorScore(performanceFactor)
                .medicalRiskScore(medicalSnapshot.medicalRiskScore())
                .medicalRiskLevel(medicalSnapshot.medicalRiskLevel())
                .medicalFlags(medicalSnapshot.medicalFlags())
                .medicalSummary(medicalSnapshot.medicalSummary())
                .overallRiskLevel(overallRiskLevel)
                .recommendations(recommendations)
                .assessedAt(assessedAt)
                .build();
    }

    // -----------------------------------------------------------------------
    // Training factor calculations (unchanged from original)
    // -----------------------------------------------------------------------

    /**
     * Calculate load factor using Acute:Chronic Workload Ratio (ACWR).
     * Acute = current week load. Chronic = 4-week rolling average.
     * Also checks for consecutive high-intensity days.
     */
    private BigDecimal calculateLoadFactor(UUID enrollmentId) {
        LocalDate now = LocalDate.now();
        LocalDate fourWeeksAgo = now.minusWeeks(4);
        LocalDate oneWeekAgo = now.minusWeeks(1);

        List<TrainingLoadRecord> recentRecords = trainingLoadRepository
                .findByEnrollmentIdAndSessionDateBetweenOrderBySessionDateDesc(
                        enrollmentId, fourWeeksAgo, now);

        if (recentRecords.isEmpty()) {
            return new BigDecimal("20.00");
        }

        BigDecimal acuteLoad = recentRecords.stream()
                .filter(r -> r.getSessionDate().isAfter(oneWeekAgo))
                .map(this::calculateSessionLoad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal chronicLoad = recentRecords.stream()
                .map(this::calculateSessionLoad)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP);

        BigDecimal acwr = BigDecimal.ZERO;
        if (chronicLoad.compareTo(BigDecimal.ZERO) > 0) {
            acwr = acuteLoad.divide(chronicLoad, 2, RoundingMode.HALF_UP);
        }

        int consecutiveHigh = countConsecutiveHighIntensityDays(recentRecords);

        BigDecimal loadRisk = BigDecimal.ZERO;

        if (acwr.compareTo(ACWR_DANGER_THRESHOLD) >= 0) {
            loadRisk = loadRisk.add(new BigDecimal("50"));
        } else if (acwr.compareTo(new BigDecimal("1.30")) >= 0) {
            loadRisk = loadRisk.add(new BigDecimal("30"));
        }

        if (consecutiveHigh >= CONSECUTIVE_HIGH_DAYS_THRESHOLD) {
            loadRisk = loadRisk.add(new BigDecimal("30"));
        } else if (consecutiveHigh >= 2) {
            loadRisk = loadRisk.add(new BigDecimal("15"));
        }

        return loadRisk.min(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate attendance factor — derive load from attendance density.
     * High session frequency without rest increases risk.
     */
    private BigDecimal calculateAttendanceFactor(UUID enrollmentId) {
        LocalDate now = LocalDate.now();
        LocalDate twoWeeksAgo = now.minusWeeks(2);

        List<TrainingLoadRecord> records = trainingLoadRepository
                .findByEnrollmentIdAndSessionDateBetweenOrderBySessionDateDesc(
                        enrollmentId, twoWeeksAgo, now);

        if (records.isEmpty()) {
            return new BigDecimal("10.00");
        }

        double sessionsPerWeek = records.size() / 2.0;

        long recoverySessions = records.stream()
                .filter(r -> r.getSessionType() == com.feadev.eaids.trainingload.model.enums.TrainingSessionType.RECOVERY)
                .count();

        BigDecimal attendanceRisk = BigDecimal.ZERO;

        if (sessionsPerWeek > 6 && recoverySessions == 0) {
            attendanceRisk = new BigDecimal("60.00");
        } else if (sessionsPerWeek > 5) {
            attendanceRisk = new BigDecimal("35.00");
        } else if (sessionsPerWeek > 4) {
            attendanceRisk = new BigDecimal("15.00");
        }

        return attendanceRisk.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate performance factor — if latest scores dropped significantly
     * compared to previous camp, it could indicate overtraining.
     */
    private BigDecimal calculatePerformanceFactor(UUID athleteUserId, UUID currentEnrollmentId) {
        List<CampEnrollment> enrollments = campEnrollmentRepository.findByUserId(athleteUserId);

        if (enrollments.size() < 2) {
            return new BigDecimal("10.00");
        }

        List<AthleteScore> scores = enrollments.stream()
                .map(e -> athleteScoreRepository.findByEnrollmentId(e.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .sorted(Comparator.comparing(AthleteScore::getCalculatedAt))
                .collect(Collectors.toList());

        if (scores.size() < 2) {
            return new BigDecimal("10.00");
        }

        AthleteScore previous = scores.get(scores.size() - 2);
        AthleteScore latestScore = scores.get(scores.size() - 1);

        if (previous.getTotalWeightedScore() == null || latestScore.getTotalWeightedScore() == null
                || previous.getTotalWeightedScore().compareTo(BigDecimal.ZERO) == 0) {
            return new BigDecimal("10.00");
        }

        BigDecimal drop = previous.getTotalWeightedScore().subtract(latestScore.getTotalWeightedScore())
                .divide(previous.getTotalWeightedScore(), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));

        if (drop.compareTo(PERFORMANCE_DROP_THRESHOLD) >= 0) {
            return new BigDecimal("60.00");
        } else if (drop.compareTo(new BigDecimal("10.00")) >= 0) {
            return new BigDecimal("35.00");
        } else if (drop.compareTo(new BigDecimal("5.00")) >= 0) {
            return new BigDecimal("15.00");
        }

        return new BigDecimal("5.00");
    }

    private BigDecimal calculateSessionLoad(TrainingLoadRecord record) {
        BigDecimal intensityMultiplier = switch (record.getIntensity()) {
            case LOW      -> new BigDecimal("1.0");
            case MEDIUM   -> new BigDecimal("2.0");
            case HIGH     -> new BigDecimal("3.0");
            case VERY_HIGH -> new BigDecimal("4.0");
        };
        return new BigDecimal(record.getDurationMinutes()).multiply(intensityMultiplier);
    }

    private int countConsecutiveHighIntensityDays(List<TrainingLoadRecord> records) {
        List<TrainingLoadRecord> sorted = records.stream()
                .sorted(Comparator.comparing(TrainingLoadRecord::getSessionDate))
                .toList();

        int maxConsecutive = 0;
        int current = 0;
        LocalDate lastDate = null;

        for (TrainingLoadRecord record : sorted) {
            if (record.getIntensity() == TrainingIntensity.HIGH
                    || record.getIntensity() == TrainingIntensity.VERY_HIGH) {
                if (lastDate == null || record.getSessionDate().equals(lastDate.plusDays(1))) {
                    current++;
                } else if (!record.getSessionDate().equals(lastDate)) {
                    current = 1;
                }
                lastDate = record.getSessionDate();
            } else {
                maxConsecutive = Math.max(maxConsecutive, current);
                current = 0;
                lastDate = null;
            }
        }
        return Math.max(maxConsecutive, current);
    }

    private int calculateCompositeRiskScore(BigDecimal loadFactor, BigDecimal attendanceFactor,
                                             BigDecimal performanceFactor) {
        BigDecimal composite = loadFactor.multiply(new BigDecimal("0.50"))
                .add(attendanceFactor.multiply(new BigDecimal("0.25")))
                .add(performanceFactor.multiply(new BigDecimal("0.25")));

        return composite.setScale(0, RoundingMode.HALF_UP).intValue();
    }

    private RiskLevel classifyRiskLevel(int score) {
        if (score >= 75) return RiskLevel.CRITICAL;
        if (score >= 50) return RiskLevel.HIGH;
        if (score >= 30) return RiskLevel.MODERATE;
        return RiskLevel.LOW;
    }

    private List<String> generateRecommendations(BigDecimal loadFactor, BigDecimal attendanceFactor,
                                                  BigDecimal performanceFactor) {
        List<String> recommendations = new ArrayList<>();

        if (loadFactor.compareTo(new BigDecimal("50")) >= 0) {
            recommendations.add("Reduce training intensity for 3-5 days");
            recommendations.add("Schedule mandatory recovery session");
        } else if (loadFactor.compareTo(new BigDecimal("30")) >= 0) {
            recommendations.add("Consider reducing training volume this week");
        }

        if (attendanceFactor.compareTo(new BigDecimal("35")) >= 0) {
            recommendations.add("Include at least 1 recovery day per week");
            recommendations.add("Limit high-intensity sessions to 4 per week");
        }

        if (performanceFactor.compareTo(new BigDecimal("35")) >= 0) {
            recommendations.add("Investigate potential overtraining — performance declining");
            recommendations.add("Consider periodization adjustments");
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Continue current training program — no concerns identified");
        }

        return recommendations;
    }

    private InjuryRiskResponse mapToResponse(InjuryRiskAssessment assessment,
                                              List<String> recommendations,
                                              MedicalRiskSnapshot medicalSnapshot,
                                              RiskLevel overallRiskLevel) {
        return InjuryRiskResponse.builder()
                .id(assessment.getId())
                .athleteUserId(assessment.getAthleteUserId())
                .enrollmentId(assessment.getEnrollmentId())
                .riskScore(assessment.getRiskScore())
                .riskLevel(assessment.getRiskLevel())
                .loadFactorScore(assessment.getLoadFactorScore())
                .attendanceFactorScore(assessment.getAttendanceFactorScore())
                .performanceFactorScore(assessment.getPerformanceFactorScore())
                .recommendations(recommendations)
                .assessedAt(assessment.getAssessedAt())
                .medicalRiskScore(medicalSnapshot.medicalRiskScore())
                .medicalRiskLevel(medicalSnapshot.medicalRiskLevel())
                .medicalFlags(medicalSnapshot.medicalFlags())
                .medicalSummary(medicalSnapshot.medicalSummary())
                .overallRiskLevel(overallRiskLevel)
                .build();
    }
}
