package com.feadev.eaids.analytics.benchmarking.service;

import com.feadev.eaids.analytics.benchmarking.dto.request.CreateBenchmarkProfileRequest;
import com.feadev.eaids.analytics.benchmarking.dto.response.BenchmarkComparisonResponse;
import com.feadev.eaids.analytics.benchmarking.dto.response.BenchmarkProfileResponse;
import com.feadev.eaids.analytics.benchmarking.model.enums.BenchmarkType;
import com.feadev.eaids.technical.model.enums.FootballPosition;

import java.util.List;
import java.util.UUID;

public interface BenchmarkingService {

    BenchmarkProfileResponse createBenchmarkProfile(CreateBenchmarkProfileRequest request);

    List<BenchmarkProfileResponse> getAllBenchmarkProfiles();

    BenchmarkProfileResponse getBenchmarkProfile(UUID id);

    BenchmarkProfileResponse updateBenchmarkProfile(UUID id, CreateBenchmarkProfileRequest request);

    void deleteBenchmarkProfile(UUID id);

    BenchmarkComparisonResponse compareAgainstBenchmark(UUID enrollmentId, BenchmarkType type, FootballPosition position);

    /**
     * Auto-capture benchmark from a TIER_1_ELITE selection.
     * Called via event listener.
     */
    void autoCaptureFromEliteSelection(UUID enrollmentId);
}
