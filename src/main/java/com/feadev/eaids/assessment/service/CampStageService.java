package com.feadev.eaids.assessment.service;

import com.feadev.eaids.assessment.dto.request.CreateStageRequest;
import com.feadev.eaids.assessment.dto.request.ReorderStagesRequest;
import com.feadev.eaids.assessment.dto.request.UpdateStageRequest;
import com.feadev.eaids.assessment.dto.response.StageResponse;

import java.util.List;
import java.util.UUID;

public interface CampStageService {

    StageResponse createStage(UUID campId, CreateStageRequest request);

    List<StageResponse> getCampStages(UUID campId);

    StageResponse getStage(UUID campId, UUID stageId);

    StageResponse updateStage(UUID campId, UUID stageId, UpdateStageRequest request);

    void deleteStage(UUID campId, UUID stageId);

    List<StageResponse> reorderStages(UUID campId, ReorderStagesRequest request);
}
