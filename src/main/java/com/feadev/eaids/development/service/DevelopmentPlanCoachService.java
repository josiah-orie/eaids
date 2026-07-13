package com.feadev.eaids.development.service;

import com.feadev.eaids.development.dto.request.*;
import com.feadev.eaids.development.dto.response.DevelopmentPlanResponse;
import com.feadev.eaids.development.dto.response.ProgressResponse;

import java.util.List;
import java.util.UUID;

public interface DevelopmentPlanCoachService {

    DevelopmentPlanResponse updatePlan(UUID planId, UpdatePlanRequest request);

    DevelopmentPlanResponse addStrength(UUID planId, AddStrengthRequest request);

    void removeStrength(UUID planId, UUID strengthId);

    DevelopmentPlanResponse addWeakness(UUID planId, AddWeaknessRequest request);

    void removeWeakness(UUID planId, UUID weaknessId);

    DevelopmentPlanResponse assignRecommendation(UUID planId, AssignRecommendationRequest request);

    void removeRecommendation(UUID planId, UUID planRecommendationId);

    DevelopmentPlanResponse addGoal(UUID planId, AddGoalRequest request);

    DevelopmentPlanResponse updateGoal(UUID planId, UUID goalId, UpdateGoalRequest request);

    ProgressResponse recordProgress(UUID planId, RecordProgressRequest request, UUID recordedBy);

    List<ProgressResponse> getProgress(UUID planId);
}
