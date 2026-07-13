package com.feadev.eaids.development.service;

import com.feadev.eaids.development.dto.request.GeneratePlanRequest;
import com.feadev.eaids.development.dto.response.DevelopmentPlanResponse;
import com.feadev.eaids.development.model.enums.DevelopmentPlanStatus;

import java.util.List;
import java.util.UUID;

public interface DevelopmentPlanService {

    DevelopmentPlanResponse generatePlan(GeneratePlanRequest request);

    DevelopmentPlanResponse getPlan(UUID planId);

    List<DevelopmentPlanResponse> getPlans(UUID athleteId, DevelopmentPlanStatus status);
}
