package com.feadev.eaids.physical.controller;

import com.feadev.eaids.physical.dto.request.CreateBenchmarkRequest;
import com.feadev.eaids.physical.dto.request.CreateMetricRequest;
import com.feadev.eaids.physical.dto.request.RecordMeasurementRequest;
import com.feadev.eaids.physical.dto.response.BenchmarkResponse;
import com.feadev.eaids.physical.dto.response.MeasurementResponse;
import com.feadev.eaids.physical.dto.response.MetricResponse;
import com.feadev.eaids.physical.service.PhysicalAssessmentService;
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
@RequestMapping("/api/v1/camps/{campId}/physical")
@RequiredArgsConstructor
public class PhysicalAssessmentController {

    private final PhysicalAssessmentService service;

    // --- Metrics ---
    @PostMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MetricResponse> createMetric(
            @PathVariable UUID campId,
            @Valid @RequestBody CreateMetricRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createMetric(campId, request));
    }

    @GetMapping("/metrics")
    public ResponseEntity<List<MetricResponse>> getCampMetrics(
            @PathVariable UUID campId) {
        return ResponseEntity.ok(service.getCampMetrics(campId));
    }

    @DeleteMapping("/metrics/{metricId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMetric(
            @PathVariable UUID campId,
            @PathVariable UUID metricId) {
        service.deleteMetric(metricId);
        return ResponseEntity.noContent().build();
    }

    // --- Benchmarks ---
    @PostMapping("/metrics/{metricId}/benchmarks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BenchmarkResponse> createBenchmark(
            @PathVariable UUID campId,
            @PathVariable UUID metricId,
            @Valid @RequestBody CreateBenchmarkRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createBenchmark(metricId, request));
    }

    @GetMapping("/metrics/{metricId}/benchmarks")
    public ResponseEntity<List<BenchmarkResponse>> getMetricBenchmarks(
            @PathVariable UUID campId,
            @PathVariable UUID metricId) {
        return ResponseEntity.ok(service.getMetricBenchmarks(metricId));
    }

    @DeleteMapping("/benchmarks/{benchmarkId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBenchmark(
            @PathVariable UUID campId,
            @PathVariable UUID benchmarkId) {
        service.deleteBenchmark(benchmarkId);
        return ResponseEntity.noContent().build();
    }

    // --- Measurements ---
    @PostMapping("/measurements")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<MeasurementResponse> recordMeasurement(
            @PathVariable UUID campId,
            @Valid @RequestBody RecordMeasurementRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.recordMeasurement(campId, request, userDetails.getUsername()));
    }

    @PostMapping("/measurements/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<List<MeasurementResponse>> recordBatchMeasurements(
            @PathVariable UUID campId,
            @Valid @RequestBody List<RecordMeasurementRequest> requests,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.recordBatchMeasurements(campId, requests, userDetails.getUsername()));
    }

    @GetMapping("/measurements/enrollment/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<List<MeasurementResponse>> getMeasurementsByEnrollment(
            @PathVariable UUID campId,
            @PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(service.getMeasurementsByEnrollment(enrollmentId));
    }

    @GetMapping("/score/enrollment/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<BigDecimal> getPhysicalScore(
            @PathVariable UUID campId,
            @PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(service.getPhysicalScore(enrollmentId));
    }
}
