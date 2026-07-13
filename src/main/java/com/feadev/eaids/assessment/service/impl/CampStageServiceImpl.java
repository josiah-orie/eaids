package com.feadev.eaids.assessment.service.impl;

import com.feadev.eaids.assessment.dto.request.CreateStageRequest;
import com.feadev.eaids.assessment.dto.request.ReorderStagesRequest;
import com.feadev.eaids.assessment.dto.request.UpdateStageRequest;
import com.feadev.eaids.assessment.dto.response.StageResponse;
import com.feadev.eaids.assessment.model.CampStage;
import com.feadev.eaids.assessment.repository.CampStageRepository;
import com.feadev.eaids.assessment.service.CampStageService;
import com.feadev.eaids.camp.model.Camp;
import com.feadev.eaids.camp.repository.CampRepository;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CampStageServiceImpl implements CampStageService {

    private final CampStageRepository stageRepository;
    private final CampRepository campRepository;

    @Override
    public StageResponse createStage(UUID campId, CreateStageRequest request) {
        Camp camp = campRepository.findById(campId)
                .orElseThrow(() -> new ResourceNotFoundException("Camp not found with id: " + campId));

        int nextOrder = stageRepository.countByCampId(campId) + 1;

        CampStage stage = CampStage.builder()
                .camp(camp)
                .stageName(request.getStageName())
                .stageOrder(nextOrder)
                .description(request.getDescription())
                .durationDays(request.getDurationDays())
                .scoreWeight(request.getScoreWeight())
                .isActive(true)
                .build();

        CampStage saved = stageRepository.save(stage);
        log.info("Stage '{}' created for camp {} at order {}", saved.getStageName(), campId, nextOrder);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StageResponse> getCampStages(UUID campId) {
        if (!campRepository.existsById(campId)) {
            throw new ResourceNotFoundException("Camp not found with id: " + campId);
        }
        return stageRepository.findByCampIdOrderByStageOrderAsc(campId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StageResponse getStage(UUID campId, UUID stageId) {
        CampStage stage = findStageOrThrow(stageId, campId);
        return mapToResponse(stage);
    }

    @Override
    public StageResponse updateStage(UUID campId, UUID stageId, UpdateStageRequest request) {
        CampStage stage = findStageOrThrow(stageId, campId);

        if (request.getStageName() != null) {
            stage.setStageName(request.getStageName());
        }
        if (request.getDescription() != null) {
            stage.setDescription(request.getDescription());
        }
        if (request.getDurationDays() != null) {
            stage.setDurationDays(request.getDurationDays());
        }
        if (request.getScoreWeight() != null) {
            stage.setScoreWeight(request.getScoreWeight());
        }
        if (request.getIsActive() != null) {
            stage.setActive(request.getIsActive());
        }

        CampStage saved = stageRepository.save(stage);
        log.info("Stage '{}' updated for camp {}", saved.getStageName(), campId);
        return mapToResponse(saved);
    }

    @Override
    public void deleteStage(UUID campId, UUID stageId) {
        CampStage stage = findStageOrThrow(stageId, campId);
        stageRepository.delete(stage);
        log.info("Stage {} deleted from camp {}", stageId, campId);
    }

    @Override
    public List<StageResponse> reorderStages(UUID campId, ReorderStagesRequest request) {
        if (!campRepository.existsById(campId)) {
            throw new ResourceNotFoundException("Camp not found with id: " + campId);
        }

        List<CampStage> stages = stageRepository.findByCampIdOrderByStageOrderAsc(campId);
        Map<UUID, CampStage> stageMap = stages.stream()
                .collect(Collectors.toMap(CampStage::getId, Function.identity()));

        List<UUID> orderedIds = request.getStageIds();
        for (int i = 0; i < orderedIds.size(); i++) {
            CampStage stage = stageMap.get(orderedIds.get(i));
            if (stage == null) {
                throw new ResourceNotFoundException("Stage not found with id: " + orderedIds.get(i));
            }
            stage.setStageOrder(i + 1);
        }

        stageRepository.saveAll(stages);
        log.info("Stages reordered for camp {}", campId);

        return stageRepository.findByCampIdOrderByStageOrderAsc(campId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CampStage findStageOrThrow(UUID stageId, UUID campId) {
        CampStage stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found with id: " + stageId));
        if (!stage.getCamp().getId().equals(campId)) {
            throw new IllegalStateException("Stage does not belong to the specified camp");
        }
        return stage;
    }

    private StageResponse mapToResponse(CampStage stage) {
        return StageResponse.builder()
                .id(stage.getId())
                .campId(stage.getCamp().getId())
                .stageName(stage.getStageName())
                .stageOrder(stage.getStageOrder())
                .description(stage.getDescription())
                .durationDays(stage.getDurationDays())
                .scoreWeight(stage.getScoreWeight())
                .isActive(stage.isActive())
                .createdDate(stage.getCreatedDate())
                .lastModifiedDate(stage.getLastModifiedDate())
                .build();
    }
}
