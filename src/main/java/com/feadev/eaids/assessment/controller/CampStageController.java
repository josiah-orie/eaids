package com.feadev.eaids.assessment.controller;

import com.feadev.eaids.assessment.dto.request.CreateStageRequest;
import com.feadev.eaids.assessment.dto.request.ReorderStagesRequest;
import com.feadev.eaids.assessment.dto.request.UpdateStageRequest;
import com.feadev.eaids.assessment.dto.response.StageResponse;
import com.feadev.eaids.assessment.service.CampStageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/camps/{campId}/stages")
@RequiredArgsConstructor
public class CampStageController {

    private final CampStageService campStageService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StageResponse> createStage(
            @PathVariable UUID campId,
            @Valid @RequestBody CreateStageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campStageService.createStage(campId, request));
    }

    @GetMapping
    public ResponseEntity<List<StageResponse>> getCampStages(@PathVariable UUID campId) {
        return ResponseEntity.ok(campStageService.getCampStages(campId));
    }

    @GetMapping("/{stageId}")
    public ResponseEntity<StageResponse> getStage(
            @PathVariable UUID campId,
            @PathVariable UUID stageId) {
        return ResponseEntity.ok(campStageService.getStage(campId, stageId));
    }

    @PutMapping("/{stageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StageResponse> updateStage(
            @PathVariable UUID campId,
            @PathVariable UUID stageId,
            @Valid @RequestBody UpdateStageRequest request) {
        return ResponseEntity.ok(campStageService.updateStage(campId, stageId, request));
    }

    @DeleteMapping("/{stageId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStage(
            @PathVariable UUID campId,
            @PathVariable UUID stageId) {
        campStageService.deleteStage(campId, stageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<StageResponse>> reorderStages(
            @PathVariable UUID campId,
            @Valid @RequestBody ReorderStagesRequest request) {
        return ResponseEntity.ok(campStageService.reorderStages(campId, request));
    }
}
