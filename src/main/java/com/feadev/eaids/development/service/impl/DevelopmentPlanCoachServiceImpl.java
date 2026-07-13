package com.feadev.eaids.development.service.impl;

import com.feadev.eaids.development.dto.request.*;
import com.feadev.eaids.development.dto.response.DevelopmentPlanResponse;
import com.feadev.eaids.development.dto.response.ProgressResponse;
import com.feadev.eaids.development.model.*;
import com.feadev.eaids.development.model.enums.GoalStatus;
import com.feadev.eaids.development.repository.*;
import com.feadev.eaids.development.service.DevelopmentPlanCoachService;
import com.feadev.eaids.development.service.DevelopmentPlanService;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DevelopmentPlanCoachServiceImpl implements DevelopmentPlanCoachService {

    private final DevelopmentPlanRepository planRepository;
    private final DevelopmentGoalRepository goalRepository;
    private final DevelopmentProgressRepository progressRepository;
    private final TrainingRecommendationRepository recommendationRepository;
    private final PlanRecommendationRepository planRecommendationRepository;
    private final DevelopmentPlanService planService;

    @Override
    public DevelopmentPlanResponse updatePlan(UUID planId, UpdatePlanRequest request) {
        DevelopmentPlan plan = findPlanOrThrow(planId);
        if (request.getOverallAssessment() != null) plan.setOverallAssessment(request.getOverallAssessment());
        if (request.getCoachUserId() != null) plan.setCoachUserId(request.getCoachUserId());
        if (request.getStatus() != null) plan.setStatus(request.getStatus());
        if (request.getStartDate() != null) plan.setStartDate(request.getStartDate());
        if (request.getTargetEndDate() != null) plan.setTargetEndDate(request.getTargetEndDate());
        planRepository.save(plan);
        return planService.getPlan(planId);
    }

    @Override
    public DevelopmentPlanResponse addStrength(UUID planId, AddStrengthRequest request) {
        DevelopmentPlan plan = findPlanOrThrow(planId);
        DevelopmentStrength strength = DevelopmentStrength.builder()
                .plan(plan).category(request.getCategory())
                .description(request.getDescription()).score(request.getScore()).build();
        plan.getStrengths().add(strength);
        planRepository.save(plan);
        return planService.getPlan(planId);
    }

    @Override
    public void removeStrength(UUID planId, UUID strengthId) {
        DevelopmentPlan plan = findPlanOrThrow(planId);
        plan.getStrengths().removeIf(s -> s.getId().equals(strengthId));
        planRepository.save(plan);
    }

    @Override
    public DevelopmentPlanResponse addWeakness(UUID planId, AddWeaknessRequest request) {
        DevelopmentPlan plan = findPlanOrThrow(planId);
        DevelopmentWeakness weakness = DevelopmentWeakness.builder()
                .plan(plan).category(request.getCategory())
                .description(request.getDescription()).score(request.getScore())
                .targetScore(request.getTargetScore()).build();
        plan.getWeaknesses().add(weakness);
        planRepository.save(plan);
        return planService.getPlan(planId);
    }

    @Override
    public void removeWeakness(UUID planId, UUID weaknessId) {
        DevelopmentPlan plan = findPlanOrThrow(planId);
        plan.getWeaknesses().removeIf(w -> w.getId().equals(weaknessId));
        planRepository.save(plan);
    }

    @Override
    public DevelopmentPlanResponse assignRecommendation(UUID planId, AssignRecommendationRequest request) {
        DevelopmentPlan plan = findPlanOrThrow(planId);
        TrainingRecommendation rec = recommendationRepository.findById(request.getRecommendationId())
                .orElseThrow(() -> new ResourceNotFoundException("Recommendation not found: " + request.getRecommendationId()));
        PlanRecommendation pr = PlanRecommendation.builder()
                .plan(plan).recommendation(rec)
                .priority(request.getPriority()).coachNotes(request.getCoachNotes()).build();
        planRecommendationRepository.save(pr);
        return planService.getPlan(planId);
    }

    @Override
    public void removeRecommendation(UUID planId, UUID planRecommendationId) {
        planRecommendationRepository.deleteById(planRecommendationId);
    }

    @Override
    public DevelopmentPlanResponse addGoal(UUID planId, AddGoalRequest request) {
        DevelopmentPlan plan = findPlanOrThrow(planId);
        DevelopmentGoal goal = DevelopmentGoal.builder()
                .plan(plan).title(request.getTitle()).description(request.getDescription())
                .metric(request.getMetric()).currentValue(request.getCurrentValue())
                .targetValue(request.getTargetValue()).targetDate(request.getTargetDate())
                .status(GoalStatus.NOT_STARTED).build();
        plan.getGoals().add(goal);
        planRepository.save(plan);
        return planService.getPlan(planId);
    }

    @Override
    public DevelopmentPlanResponse updateGoal(UUID planId, UUID goalId, UpdateGoalRequest request) {
        DevelopmentGoal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + goalId));
        if (request.getTitle() != null) goal.setTitle(request.getTitle());
        if (request.getDescription() != null) goal.setDescription(request.getDescription());
        if (request.getMetric() != null) goal.setMetric(request.getMetric());
        if (request.getCurrentValue() != null) goal.setCurrentValue(request.getCurrentValue());
        if (request.getTargetValue() != null) goal.setTargetValue(request.getTargetValue());
        if (request.getTargetDate() != null) goal.setTargetDate(request.getTargetDate());
        if (request.getStatus() != null) goal.setStatus(request.getStatus());
        goalRepository.save(goal);
        return planService.getPlan(planId);
    }

    @Override
    public ProgressResponse recordProgress(UUID planId, RecordProgressRequest request, UUID recordedBy) {
        DevelopmentPlan plan = findPlanOrThrow(planId);
        DevelopmentGoal goal = null;
        if (request.getGoalId() != null) {
            goal = goalRepository.findById(request.getGoalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Goal not found: " + request.getGoalId()));
        }
        DevelopmentProgress progress = DevelopmentProgress.builder()
                .plan(plan).goal(goal).value(request.getValue())
                .notes(request.getNotes()).recordedBy(recordedBy)
                .recordedDate(request.getRecordedDate()).build();
        DevelopmentProgress saved = progressRepository.save(progress);
        return mapProgressToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProgressResponse> getProgress(UUID planId) {
        return progressRepository.findByPlanIdOrderByRecordedDateDesc(planId).stream()
                .map(this::mapProgressToResponse).collect(Collectors.toList());
    }

    private DevelopmentPlan findPlanOrThrow(UUID planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Development plan not found: " + planId));
    }

    private ProgressResponse mapProgressToResponse(DevelopmentProgress p) {
        return ProgressResponse.builder()
                .id(p.getId()).planId(p.getPlan().getId())
                .goalId(p.getGoal() != null ? p.getGoal().getId() : null)
                .goalTitle(p.getGoal() != null ? p.getGoal().getTitle() : null)
                .value(p.getValue()).notes(p.getNotes())
                .recordedBy(p.getRecordedBy()).recordedDate(p.getRecordedDate()).build();
    }
}
