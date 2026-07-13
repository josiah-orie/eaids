package com.feadev.eaids.physical.service;

import com.feadev.eaids.physical.dto.request.CreateBenchmarkRequest;
import com.feadev.eaids.physical.dto.request.CreateMetricRequest;
import com.feadev.eaids.physical.dto.request.RecordMeasurementRequest;
import com.feadev.eaids.physical.dto.response.BenchmarkResponse;
import com.feadev.eaids.physical.dto.response.MeasurementResponse;
import com.feadev.eaids.physical.dto.response.MetricResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PhysicalAssessmentService {

    // Metrics
    MetricResponse createMetric(UUID campId, CreateMetricRequest request);
    List<MetricResponse> getCampMetrics(UUID campId);
    void deleteMetric(UUID metricId);

    // Benchmarks
    BenchmarkResponse createBenchmark(UUID metricId, CreateBenchmarkRequest request);
    List<BenchmarkResponse> getMetricBenchmarks(UUID metricId);
    void deleteBenchmark(UUID benchmarkId);

    // Measurements
    MeasurementResponse recordMeasurement(UUID campId, RecordMeasurementRequest request, String recordedBy);
    List<MeasurementResponse> recordBatchMeasurements(UUID campId, List<RecordMeasurementRequest> requests, String recordedBy);
    List<MeasurementResponse> getMeasurementsByEnrollment(UUID enrollmentId);
    BigDecimal getPhysicalScore(UUID enrollmentId);
}
