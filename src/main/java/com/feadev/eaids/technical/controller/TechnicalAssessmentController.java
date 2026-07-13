package com.feadev.eaids.technical.controller;

import com.feadev.eaids.technical.dto.request.CreateTechnicalMetricRequest;
import com.feadev.eaids.technical.dto.request.RecordTechnicalScoreRequest;
import com.feadev.eaids.technical.dto.response.TechnicalMetricResponse;
import com.feadev.eaids.technical.dto.response.TechnicalScoreResponse;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import com.feadev.eaids.technical.service.TechnicalAssessmentService;
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
@RequestMapping("/api/v1/camps/{campId}/technical")
@RequiredArgsConstructor
public class TechnicalAssessmentController {

    private final TechnicalAssessmentService service;

    @PostMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TechnicalMetricResponse> createMetric(
            @PathVariable UUID campId,
            @Valid @RequestBody CreateTechnicalMetricRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createMetric(campId, request));
    }

    @GetMapping("/metrics")
    public ResponseEntity<List<TechnicalMetricResponse>> getCampMetrics(
            @PathVariable UUID campId,
            @RequestParam(required = false) FootballPosition position) {
        return ResponseEntity.ok(service.getCampMetrics(campId, position));
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
    public ResponseEntity<TechnicalScoreResponse> recordScore(
            @PathVariable UUID campId,
            @Valid @RequestBody RecordTechnicalScoreRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID evaluatorId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.recordScore(campId, request, evaluatorId));
    }

    @GetMapping("/scores/enrollment/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<List<TechnicalScoreResponse>> getScoresByEnrollment(
            @PathVariable UUID campId, @PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(service.getScoresByEnrollment(enrollmentId));
    }

    @GetMapping("/scores/enrollment/{enrollmentId}/weighted")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<BigDecimal> getWeightedScore(
            @PathVariable UUID campId,
            @PathVariable UUID enrollmentId,
            @RequestParam FootballPosition position) {
        return ResponseEntity.ok(service.getWeightedTechnicalScore(enrollmentId, position));
    }
}
