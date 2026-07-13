package com.feadev.eaids.trainingload.controller;

import com.feadev.eaids.trainingload.dto.request.CreateTrainingLoadRequest;
import com.feadev.eaids.trainingload.dto.response.TrainingLoadResponse;
import com.feadev.eaids.trainingload.service.TrainingLoadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/camps/{campId}/enrollments/{enrollmentId}/training-load")
@RequiredArgsConstructor
public class TrainingLoadController {

    private final TrainingLoadService trainingLoadService;

    @PostMapping
    @PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
    public ResponseEntity<TrainingLoadResponse> recordTrainingLoad(
            @PathVariable UUID campId,
            @PathVariable UUID enrollmentId,
            @Valid @RequestBody CreateTrainingLoadRequest request) {
        TrainingLoadResponse response = trainingLoadService.recordTrainingLoad(campId, enrollmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
    public ResponseEntity<Page<TrainingLoadResponse>> getTrainingLoadHistory(
            @PathVariable UUID campId,
            @PathVariable UUID enrollmentId,
            Pageable pageable) {
        Page<TrainingLoadResponse> history = trainingLoadService.getTrainingLoadHistory(enrollmentId, pageable);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{recordId}")
    @PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
    public ResponseEntity<TrainingLoadResponse> getTrainingLoadById(
            @PathVariable UUID campId,
            @PathVariable UUID enrollmentId,
            @PathVariable UUID recordId) {
        return ResponseEntity.ok(trainingLoadService.getTrainingLoadById(recordId));
    }

    @DeleteMapping("/{recordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTrainingLoad(
            @PathVariable UUID campId,
            @PathVariable UUID enrollmentId,
            @PathVariable UUID recordId) {
        trainingLoadService.deleteTrainingLoad(recordId);
        return ResponseEntity.noContent().build();
    }
}
