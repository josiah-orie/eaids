package com.feadev.eaids.development.service.impl;

import com.feadev.eaids.development.dto.request.GeneratePlanRequest;
import com.feadev.eaids.development.dto.response.DevelopmentPlanResponse;
import com.feadev.eaids.development.model.DevelopmentPlan;
import com.feadev.eaids.development.model.DevelopmentStrength;
import com.feadev.eaids.development.model.DevelopmentWeakness;
import com.feadev.eaids.development.model.enums.DevelopmentPlanStatus;
import com.feadev.eaids.development.repository.DevelopmentPlanRepository;
import com.feadev.eaids.development.service.DevelopmentPlanService;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.scoring.model.AthleteScore;
import com.feadev.eaids.scoring.repository.AthleteScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DevelopmentPlanServiceImpl implements DevelopmentPlanService {

    private final DevelopmentPlanRepository planRepository;
    private final AthleteScoreRepository athleteScoreRepository;
    private final CampEnrollmentRepository enrollmentRepository;
    private final AthleteProfileRepository athleteProfileRepository;

    private static final BigDecimal STRENGTH_THRESHOLD = new BigDecimal("80.00");
    private static final BigDecimal WEAKNESS_THRESHOLD = new BigDecimal("60.00");

    @Override
    public DevelopmentPlanResponse generatePlan(GeneratePlanRequest request) {
        // Find the enrollment for this athlete in this camp
        CampEnrollment enrollment = enrollmentRepository.findByCampIdAndUserId(
                        request.getCampId(), request.getAthleteUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found for athlete " + request.getAthleteUserId() + " in camp " + request.getCampId()));

        // Get the athlete's score
        AthleteScore score = athleteScoreRepository.findByEnrollmentId(enrollment.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Score not found for enrollment: " + enrollment.getId()));

        // Create plan
        DevelopmentPlan plan = DevelopmentPlan.builder()
                .athleteUserId(request.getAthleteUserId())
                .campId(request.getCampId())
                .status(DevelopmentPlanStatus.DRAFT)
                .build();

        // Analyze scores and populate strengths/weaknesses
        List<DevelopmentStrength> strengths = new ArrayList<>();
        List<DevelopmentWeakness> weaknesses = new ArrayList<>();

        analyzeCategory("Physical", score.getPhysicalScore(), plan, strengths, weaknesses);
        analyzeCategory("Technical", score.getTechnicalScore(), plan, strengths, weaknesses);
        analyzeCategory("Tactical", score.getTacticalScore(), plan, strengths, weaknesses);
        analyzeCategory("Attendance", score.getAttendanceScore(), plan, strengths, weaknesses);
        analyzeCategory("Discipline", score.getDisciplineScore(), plan, strengths, weaknesses);
        analyzeCategory("Coach Review", score.getCoachReviewScore(), plan, strengths, weaknesses);
        if (score.getMatchStatsScore() != null) {
            analyzeCategory("Match Stats", score.getMatchStatsScore(), plan, strengths, weaknesses);
        }

        plan.setStrengths(strengths);
        plan.setWeaknesses(weaknesses);

        DevelopmentPlan saved = planRepository.save(plan);
        log.info("Development plan generated for athlete {} in camp {}", request.getAthleteUserId(), request.getCampId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DevelopmentPlanResponse getPlan(UUID planId) {
        DevelopmentPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Development plan not found: " + planId));
        return mapToResponse(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DevelopmentPlanResponse> getPlans(UUID athleteId, DevelopmentPlanStatus status) {
        List<DevelopmentPlan> plans;
        if (athleteId != null && status != null) {
            plans = planRepository.findByAthleteUserIdAndStatus(athleteId, status);
        } else if (athleteId != null) {
            plans = planRepository.findByAthleteUserId(athleteId);
        } else if (status != null) {
            plans = planRepository.findByStatus(status);
        } else {
            plans = planRepository.findAll();
        }
        return plans.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private void analyzeCategory(String category, BigDecimal score, DevelopmentPlan plan,
                                  List<DevelopmentStrength> strengths, List<DevelopmentWeakness> weaknesses) {
        if (score == null) return;

        if (score.compareTo(STRENGTH_THRESHOLD) >= 0) {
            strengths.add(DevelopmentStrength.builder()
                    .plan(plan)
                    .category(category)
                    .description(category + ": " + score.toPlainString() + " — Strength")
                    .score(score)
                    .build());
        } else if (score.compareTo(WEAKNESS_THRESHOLD) < 0) {
            weaknesses.add(DevelopmentWeakness.builder()
                    .plan(plan)
                    .category(category)
                    .description(category + ": " + score.toPlainString() + " — Weakness")
                    .score(score)
                    .targetScore(WEAKNESS_THRESHOLD.toPlainString())
                    .build());
        }
    }

    private DevelopmentPlanResponse mapToResponse(DevelopmentPlan plan) {
        String athleteName = athleteProfileRepository.findByUser_Id(plan.getAthleteUserId())
                .map(p -> p.getFullName())
                .orElse("Unknown");

        List<DevelopmentPlanResponse.StrengthResponse> strengthResponses = plan.getStrengths().stream()
                .map(s -> DevelopmentPlanResponse.StrengthResponse.builder()
                        .id(s.getId())
                        .category(s.getCategory())
                        .description(s.getDescription())
                        .score(s.getScore())
                        .build())
                .collect(Collectors.toList());

        List<DevelopmentPlanResponse.WeaknessResponse> weaknessResponses = plan.getWeaknesses().stream()
                .map(w -> DevelopmentPlanResponse.WeaknessResponse.builder()
                        .id(w.getId())
                        .category(w.getCategory())
                        .description(w.getDescription())
                        .score(w.getScore())
                        .targetScore(w.getTargetScore())
                        .build())
                .collect(Collectors.toList());

        List<DevelopmentPlanResponse.GoalResponse> goalResponses = plan.getGoals().stream()
                .map(g -> DevelopmentPlanResponse.GoalResponse.builder()
                        .id(g.getId())
                        .title(g.getTitle())
                        .description(g.getDescription())
                        .metric(g.getMetric())
                        .currentValue(g.getCurrentValue())
                        .targetValue(g.getTargetValue())
                        .targetDate(g.getTargetDate())
                        .status(g.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        return DevelopmentPlanResponse.builder()
                .id(plan.getId())
                .athleteUserId(plan.getAthleteUserId())
                .athleteName(athleteName)
                .campId(plan.getCampId())
                .coachUserId(plan.getCoachUserId())
                .status(plan.getStatus().name())
                .startDate(plan.getStartDate())
                .targetEndDate(plan.getTargetEndDate())
                .overallAssessment(plan.getOverallAssessment())
                .strengths(strengthResponses)
                .weaknesses(weaknessResponses)
                .goals(goalResponses)
                .createdDate(plan.getCreatedDate())
                .lastModifiedDate(plan.getLastModifiedDate())
                .build();
    }
}
