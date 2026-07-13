package com.feadev.eaids.tactical.controller;

import com.feadev.eaids.tactical.dto.request.CreateTacticalMetricRequest;
import com.feadev.eaids.tactical.dto.request.RecordTacticalScoreRequest;
import com.feadev.eaids.tactical.dto.response.TacticalMetricResponse;
import com.feadev.eaids.tactical.dto.response.TacticalScoreResponse;
import com.feadev.eaids.tactical.service.TacticalAssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/camps/{campId}/tactical")
@RequiredArgsConstructor
public class TacticalAssessmentController {

    private final TacticalAssessmentService service;

    @PostMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TacticalMetricResponse> createMetric(
            @PathVariable UUID campId,
            @Valid @RequestBody CreateTacticalMetricRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createMetric(campId, request));
    }

    @GetMapping("/metrics")
    public ResponseEntity<List<TacticalMetricResponse>> getCampMetrics(
            @PathVariable UUID campId) {
        return ResponseEntity.ok(service.getCampMetrics(campId));
    }

    @DeleteMapping("/metrics/{metricId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMetric(
            @PathVariable UUID campId, @PathVariable UUID metricId) {
        service.deleteMetric(metricId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/scores")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<TacticalScoreResponse> recordScore(
            @PathVariable UUID campId,
            @Valid @RequestBody RecordTacticalScoreRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID evaluatorId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.recordScore(campId, request, evaluatorId));
    }

    @GetMapping("/scores/enrollment/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<List<TacticalScoreResponse>> getScoresByEnrollment(
            @PathVariable UUID campId, @PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(service.getScoresByEnrollment(enrollmentId));
    }

    @GetMapping("/scores/enrollment/{enrollmentId}/weighted")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<BigDecimal> getWeightedScore(
            @PathVariable UUID campId, @PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(service.getWeightedTacticalScore(enrollmentId));
    }
}
