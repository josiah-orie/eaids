package com.feadev.eaids.scoring.service.impl;

import com.feadev.eaids.attendance.dto.response.AttendanceSummaryResponse;
import com.feadev.eaids.attendance.service.AttendanceService;
import com.feadev.eaids.camp.model.Camp;
import com.feadev.eaids.camp.repository.CampRepository;
import com.feadev.eaids.discipline.dto.response.DisciplineSummaryResponse;
import com.feadev.eaids.discipline.service.DisciplineService;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.model.enums.EnrollmentStatus;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.evaluation.service.EvaluationService;
import com.feadev.eaids.matchstats.service.MatchStatsScoreCalculator;
import com.feadev.eaids.physical.service.PhysicalAssessmentService;
import com.feadev.eaids.positionprofile.model.CampPositionProfile;
import com.feadev.eaids.positionprofile.model.PositionWeightTemplate;
import com.feadev.eaids.positionprofile.repository.CampPositionProfileRepository;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.scoring.dto.request.CreateWeightProfileRequest;
import com.feadev.eaids.scoring.dto.response.AthleteScoreResponse;
import com.feadev.eaids.scoring.dto.response.WeightProfileResponse;
import com.feadev.eaids.scoring.event.ScoresCalculatedEvent;
import com.feadev.eaids.scoring.model.AthleteScore;
import com.feadev.eaids.scoring.model.CampWeightProfile;
import com.feadev.eaids.scoring.repository.AthleteScoreRepository;
import com.feadev.eaids.scoring.repository.CampWeightProfileRepository;
import com.feadev.eaids.scoring.service.ScoringService;
import com.feadev.eaids.tactical.service.TacticalAssessmentService;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import com.feadev.eaids.technical.service.TechnicalAssessmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ScoringServiceImpl implements ScoringService {

    private final CampWeightProfileRepository weightProfileRepository;
    private final AthleteScoreRepository athleteScoreRepository;
    private final CampRepository campRepository;
    private final CampEnrollmentRepository enrollmentRepository;
    private final AttendanceService attendanceService;
    private final DisciplineService disciplineService;
    private final PhysicalAssessmentService physicalAssessmentService;
    private final TechnicalAssessmentService technicalAssessmentService;
    private final TacticalAssessmentService tacticalAssessmentService;
    private final EvaluationService evaluationService;
    private final AthleteProfileRepository athleteProfileRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CampPositionProfileRepository campPositionProfileRepository;
    private final MatchStatsScoreCalculator matchStatsScoreCalculator;

    @Override
    public WeightProfileResponse createWeightProfile(UUID campId, CreateWeightProfileRequest request) {
        Camp camp = campRepository.findById(campId)
                .orElseThrow(() -> new ResourceNotFoundException("Camp not found: " + campId));

        // Validate weights sum to 100
        BigDecimal total = BigDecimal.ZERO;
        if (request.getPhysicalWeight() != null) total = total.add(request.getPhysicalWeight());
        if (request.getTechnicalWeight() != null) total = total.add(request.getTechnicalWeight());
        if (request.getTacticalWeight() != null) total = total.add(request.getTacticalWeight());
        if (request.getAttendanceWeight() != null) total = total.add(request.getAttendanceWeight());
        if (request.getDisciplineWeight() != null) total = total.add(request.getDisciplineWeight());
        if (request.getCoachReviewWeight() != null) total = total.add(request.getCoachReviewWeight());
        if (request.getMatchStatsWeight() != null) total = total.add(request.getMatchStatsWeight());

        if (total.compareTo(new BigDecimal("100.00")) != 0) {
            throw new IllegalArgumentException("Weights must sum to 100. Current total: " + total);
        }

        CampWeightProfile profile = CampWeightProfile.builder()
                .camp(camp)
                .position(request.getPosition())
                .physicalWeight(request.getPhysicalWeight())
                .technicalWeight(request.getTechnicalWeight())
                .tacticalWeight(request.getTacticalWeight())
                .attendanceWeight(request.getAttendanceWeight())
                .disciplineWeight(request.getDisciplineWeight())
                .coachReviewWeight(request.getCoachReviewWeight())
                .matchStatsWeight(request.getMatchStatsWeight() != null ? request.getMatchStatsWeight() : BigDecimal.ZERO)
                .build();

        CampWeightProfile saved = weightProfileRepository.save(profile);
        log.info("Weight profile created for camp {} position {}", campId, request.getPosition());
        return mapWeightProfileToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeightProfileResponse> getWeightProfiles(UUID campId) {
        return weightProfileRepository.findByCampId(campId).stream()
                .map(this::mapWeightProfileToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AthleteScoreResponse> calculateScores(UUID campId) {
        campRepository.findById(campId)
                .orElseThrow(() -> new ResourceNotFoundException("Camp not found: " + campId));

        List<CampEnrollment> enrollments = enrollmentRepository.findByCampIdAndStatus(campId, EnrollmentStatus.CONFIRMED);
        List<AthleteScoreResponse> responses = new ArrayList<>();

        for (CampEnrollment enrollment : enrollments) {
            try {
                AthleteScore score = calculateScoreForEnrollment(enrollment, campId);
                responses.add(mapAthleteScoreToResponse(score, enrollment));
            } catch (Exception e) {
                log.warn("Failed to calculate score for enrollment {}: {}", enrollment.getId(), e.getMessage());
            }
        }

        eventPublisher.publishEvent(new ScoresCalculatedEvent(campId));
        log.info("Scores calculated for camp {}. Total: {}", campId, responses.size());
        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public AthleteScoreResponse getAthleteScore(UUID enrollmentId) {
        AthleteScore score = athleteScoreRepository.findByEnrollmentId(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Score not found for enrollment: " + enrollmentId));

        CampEnrollment enrollment = score.getEnrollment();
        return mapAthleteScoreToResponse(score, enrollment);
    }

    private AthleteScore calculateScoreForEnrollment(CampEnrollment enrollment, UUID campId) {
        UUID enrollmentId = enrollment.getId();
        FootballPosition position = FootballPosition.ATHLETE; // Default position

        // Get individual scores
        AttendanceSummaryResponse attendanceSummary = attendanceService.getAttendanceSummary(enrollmentId);
        BigDecimal attendanceScore = attendanceSummary.getAverageScore() != null
                ? attendanceSummary.getAverageScore() : BigDecimal.ZERO;

        DisciplineSummaryResponse disciplineSummary = disciplineService.getDisciplineSummary(enrollmentId);
        BigDecimal disciplineScore = disciplineSummary.getDisciplineScore() != null
                ? disciplineSummary.getDisciplineScore() : BigDecimal.ZERO;

        BigDecimal physicalScore = physicalAssessmentService.getPhysicalScore(enrollmentId);
        if (physicalScore == null) physicalScore = BigDecimal.ZERO;

        BigDecimal technicalScore = technicalAssessmentService.getWeightedTechnicalScore(enrollmentId, position);
        if (technicalScore == null) technicalScore = BigDecimal.ZERO;

        BigDecimal tacticalScore = tacticalAssessmentService.getWeightedTacticalScore(enrollmentId);
        if (tacticalScore == null) tacticalScore = BigDecimal.ZERO;

        BigDecimal coachReviewScore = evaluationService.getAggregatedCoachScore(enrollmentId);
        if (coachReviewScore == null) coachReviewScore = BigDecimal.ZERO;

        // Get weight profile with fallback chain:
        // 1. Camp-specific CampWeightProfile for the position
        // 2. PositionProfile weight template assigned to the camp
        // 3. Default weights
        CampWeightProfile weightProfile = weightProfileRepository.findByCampIdAndPosition(campId, position)
                .orElseGet(() -> {
                    // Try position profile template assigned to the camp
                    return campPositionProfileRepository.findByCampIdAndPosition(campId, position)
                            .map(CampPositionProfile::getPositionProfile)
                            .map(pp -> pp.getWeightTemplate())
                            .map(this::convertWeightTemplateToCampProfile)
                            .orElseGet(() -> weightProfileRepository.findByCampIdAndPosition(campId, FootballPosition.ATHLETE)
                                    .orElse(getDefaultWeightProfile()));
                });

        // Calculate match stats score (percentile-based, may be null)
        BigDecimal matchStatsScore = matchStatsScoreCalculator.calculateScore(enrollmentId, campId);

        // Calculate total weighted score
        BigDecimal totalWeightedScore = calculateWeightedTotal(
                physicalScore, technicalScore, tacticalScore,
                attendanceScore, disciplineScore, coachReviewScore,
                matchStatsScore, weightProfile
        );

        // Save or update athlete score
        AthleteScore athleteScore = athleteScoreRepository.findByEnrollmentId(enrollmentId)
                .orElse(new AthleteScore());

        athleteScore.setEnrollment(enrollment);
        athleteScore.setCampId(campId);
        athleteScore.setPosition(position);
        athleteScore.setPhysicalScore(physicalScore);
        athleteScore.setTechnicalScore(technicalScore);
        athleteScore.setTacticalScore(tacticalScore);
        athleteScore.setAttendanceScore(attendanceScore);
        athleteScore.setDisciplineScore(disciplineScore);
        athleteScore.setCoachReviewScore(coachReviewScore);
        athleteScore.setMatchStatsScore(matchStatsScore);
        athleteScore.setTotalWeightedScore(totalWeightedScore);
        athleteScore.setCalculatedAt(LocalDateTime.now());

        return athleteScoreRepository.save(athleteScore);
    }

    private BigDecimal calculateWeightedTotal(
            BigDecimal physicalScore, BigDecimal technicalScore, BigDecimal tacticalScore,
            BigDecimal attendanceScore, BigDecimal disciplineScore, BigDecimal coachReviewScore,
            BigDecimal matchStatsScore, CampWeightProfile profile) {

        BigDecimal hundred = new BigDecimal("100.00");
        BigDecimal matchStatsWeight = profile.getMatchStatsWeight() != null
                ? profile.getMatchStatsWeight() : BigDecimal.ZERO;

        BigDecimal total = physicalScore.multiply(profile.getPhysicalWeight()).divide(hundred, 2, RoundingMode.HALF_UP)
                .add(technicalScore.multiply(profile.getTechnicalWeight()).divide(hundred, 2, RoundingMode.HALF_UP))
                .add(tacticalScore.multiply(profile.getTacticalWeight()).divide(hundred, 2, RoundingMode.HALF_UP))
                .add(attendanceScore.multiply(profile.getAttendanceWeight()).divide(hundred, 2, RoundingMode.HALF_UP))
                .add(disciplineScore.multiply(profile.getDisciplineWeight()).divide(hundred, 2, RoundingMode.HALF_UP))
                .add(coachReviewScore.multiply(profile.getCoachReviewWeight()).divide(hundred, 2, RoundingMode.HALF_UP));

        // Include match stats if available and weighted
        if (matchStatsScore != null && matchStatsWeight.compareTo(BigDecimal.ZERO) > 0) {
            total = total.add(matchStatsScore.multiply(matchStatsWeight).divide(hundred, 2, RoundingMode.HALF_UP));
        } else if (matchStatsScore == null && matchStatsWeight.compareTo(BigDecimal.ZERO) > 0) {
            // Redistribute match stats weight proportionally across other categories
            BigDecimal otherWeightsSum = hundred.subtract(matchStatsWeight);
            if (otherWeightsSum.compareTo(BigDecimal.ZERO) > 0) {
                total = total.multiply(hundred).divide(otherWeightsSum, 2, RoundingMode.HALF_UP);
            }
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private CampWeightProfile convertWeightTemplateToCampProfile(PositionWeightTemplate template) {
        return CampWeightProfile.builder()
                .physicalWeight(template.getPhysicalWeight())
                .technicalWeight(template.getTechnicalWeight())
                .tacticalWeight(template.getTacticalWeight())
                .attendanceWeight(template.getAttendanceWeight())
                .disciplineWeight(template.getDisciplineWeight())
                .coachReviewWeight(template.getCoachReviewWeight())
                .matchStatsWeight(template.getMatchStatsWeight())
                .build();
    }

    private CampWeightProfile getDefaultWeightProfile() {
        return CampWeightProfile.builder()
                .physicalWeight(new BigDecimal("20.00"))
                .technicalWeight(new BigDecimal("25.00"))
                .tacticalWeight(new BigDecimal("20.00"))
                .attendanceWeight(new BigDecimal("5.00"))
                .disciplineWeight(new BigDecimal("5.00"))
                .coachReviewWeight(new BigDecimal("25.00"))
                .matchStatsWeight(new BigDecimal("0.00"))
                .build();
    }

    private WeightProfileResponse mapWeightProfileToResponse(CampWeightProfile profile) {
        return WeightProfileResponse.builder()
                .id(profile.getId())
                .campId(profile.getCamp().getId())
                .position(profile.getPosition())
                .physicalWeight(profile.getPhysicalWeight())
                .technicalWeight(profile.getTechnicalWeight())
                .tacticalWeight(profile.getTacticalWeight())
                .attendanceWeight(profile.getAttendanceWeight())
                .disciplineWeight(profile.getDisciplineWeight())
                .coachReviewWeight(profile.getCoachReviewWeight())
                .matchStatsWeight(profile.getMatchStatsWeight())
                .createdDate(profile.getCreatedDate())
                .lastModifiedDate(profile.getLastModifiedDate())
                .build();
    }

    private AthleteScoreResponse mapAthleteScoreToResponse(AthleteScore score, CampEnrollment enrollment) {
        String athleteName = athleteProfileRepository.findByUser_Id(enrollment.getUser().getId())
                .map(profile -> profile.getFullName())
                .orElse("Unknown");

        return AthleteScoreResponse.builder()
                .id(score.getId())
                .enrollmentId(enrollment.getId())
                .athleteName(athleteName)
                .campId(score.getCampId())
                .position(score.getPosition())
                .physicalScore(score.getPhysicalScore())
                .technicalScore(score.getTechnicalScore())
                .tacticalScore(score.getTacticalScore())
                .attendanceScore(score.getAttendanceScore())
                .disciplineScore(score.getDisciplineScore())
                .coachReviewScore(score.getCoachReviewScore())
                .matchStatsScore(score.getMatchStatsScore())
                .totalWeightedScore(score.getTotalWeightedScore())
                .calculatedAt(score.getCalculatedAt())
                .build();
    }
}
