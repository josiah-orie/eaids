package com.feadev.eaids.analytics.benchmarking.controller;

import com.feadev.eaids.analytics.benchmarking.dto.request.CreateBenchmarkProfileRequest;
import com.feadev.eaids.analytics.benchmarking.dto.response.BenchmarkComparisonResponse;
import com.feadev.eaids.analytics.benchmarking.dto.response.BenchmarkProfileResponse;
import com.feadev.eaids.analytics.benchmarking.model.enums.BenchmarkType;
import com.feadev.eaids.analytics.benchmarking.service.BenchmarkingService;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BenchmarkingController {

    private final BenchmarkingService benchmarkingService;

    // Admin CRUD for benchmark profiles
    @PostMapping("/api/v1/admin/benchmarks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BenchmarkProfileResponse> createBenchmark(
            @Valid @RequestBody CreateBenchmarkProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(benchmarkingService.createBenchmarkProfile(request));
    }

    @GetMapping("/api/v1/admin/benchmarks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BenchmarkProfileResponse>> getAllBenchmarks() {
        return ResponseEntity.ok(benchmarkingService.getAllBenchmarkProfiles());
    }

    @GetMapping("/api/v1/admin/benchmarks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BenchmarkProfileResponse> getBenchmark(@PathVariable UUID id) {
        return ResponseEntity.ok(benchmarkingService.getBenchmarkProfile(id));
    }

    @PutMapping("/api/v1/admin/benchmarks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BenchmarkProfileResponse> updateBenchmark(
            @PathVariable UUID id,
            @Valid @RequestBody CreateBenchmarkProfileRequest request) {
        return ResponseEntity.ok(benchmarkingService.updateBenchmarkProfile(id, request));
    }

    @DeleteMapping("/api/v1/admin/benchmarks/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBenchmark(@PathVariable UUID id) {
        benchmarkingService.deleteBenchmarkProfile(id);
        return ResponseEntity.noContent().build();
    }

    // Comparison endpoint
    @GetMapping("/api/v1/analytics/benchmarks/compare/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<BenchmarkComparisonResponse> compareBenchmark(
            @PathVariable UUID enrollmentId,
            @RequestParam BenchmarkType type,
            @RequestParam(required = false) FootballPosition position) {
        return ResponseEntity.ok(benchmarkingService.compareAgainstBenchmark(enrollmentId, type, position));
    }
}
