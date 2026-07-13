package com.feadev.eaids.physical.service.impl;

import com.feadev.eaids.camp.model.Camp;
import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.camp.repository.CampRepository;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.physical.dto.request.CreateBenchmarkRequest;
import com.feadev.eaids.physical.dto.request.CreateMetricRequest;
import com.feadev.eaids.physical.dto.request.RecordMeasurementRequest;
import com.feadev.eaids.physical.dto.response.BenchmarkResponse;
import com.feadev.eaids.physical.dto.response.MeasurementResponse;
import com.feadev.eaids.physical.dto.response.MetricResponse;
import com.feadev.eaids.physical.model.AssessmentMetric;
import com.feadev.eaids.physical.model.AthleteMeasurement;
import com.feadev.eaids.physical.model.MetricBenchmark;
import com.feadev.eaids.physical.repository.AssessmentMetricRepository;
import com.feadev.eaids.physical.repository.AthleteMeasurementRepository;
import com.feadev.eaids.physical.repository.MetricBenchmarkRepository;
import com.feadev.eaids.physical.service.PhysicalAssessmentService;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.AthleteProfile;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PhysicalAssessmentServiceImpl implements PhysicalAssessmentService {

    private final AssessmentMetricRepository metricRepository;
    private final MetricBenchmarkRepository benchmarkRepository;
    private final AthleteMeasurementRepository measurementRepository;
    private final CampRepository campRepository;
    private final CampEnrollmentRepository enrollmentRepository;
    private final AthleteProfileRepository athleteProfileRepository;

    @Override
    public MetricResponse createMetric(UUID campId, CreateMetricRequest request) {
        Camp camp = campRepository.findById(campId)
                .orElseThrow(() -> new ResourceNotFoundException("Camp not found: " + campId));

        AssessmentMetric metric = AssessmentMetric.builder()
                .camp(camp)
                .category(request.getCategory())
                .metricName(request.getMetricName())
                .unit(request.getUnit())
                .description(request.getDescription())
                .lowerIsBetter(request.isLowerIsBetter())
                .isActive(true)
                .build();

        AssessmentMetric saved = metricRepository.save(metric);
        log.info("Metric '{}' created for camp {}", saved.getMetricName(), campId);
        return mapMetricToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetricResponse> getCampMetrics(UUID campId) {
        return metricRepository.findByCampId(campId).stream()
                .map(this::mapMetricToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMetric(UUID metricId) {
        AssessmentMetric metric = metricRepository.findById(metricId)
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found: " + metricId));
        metricRepository.delete(metric);
    }

    @Override
    public BenchmarkResponse createBenchmark(UUID metricId, CreateBenchmarkRequest request) {
        AssessmentMetric metric = metricRepository.findById(metricId)
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found: " + metricId));

        MetricBenchmark benchmark = MetricBenchmark.builder()
                .metric(metric)
                .minValue(request.getMinValue())
                .maxValue(request.getMaxValue())
                .score(request.getScore())
                .ageCategory(request.getAgeCategory())
                .gender(request.getGender())
                .build();

        MetricBenchmark saved = benchmarkRepository.save(benchmark);
        log.info("Benchmark created for metric {}: score={}", metricId, saved.getScore());
        return mapBenchmarkToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BenchmarkResponse> getMetricBenchmarks(UUID metricId) {
        return benchmarkRepository.findByMetricId(metricId).stream()
                .map(this::mapBenchmarkToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBenchmark(UUID benchmarkId) {
        MetricBenchmark benchmark = benchmarkRepository.findById(benchmarkId)
                .orElseThrow(() -> new ResourceNotFoundException("Benchmark not found: " + benchmarkId));
        benchmarkRepository.delete(benchmark);
    }

    @Override
    public MeasurementResponse recordMeasurement(UUID campId, RecordMeasurementRequest request, String recordedBy) {
        CampEnrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + request.getEnrollmentId()));

        if (!enrollment.getCamp().getId().equals(campId)) {
            throw new IllegalStateException("Enrollment does not belong to this camp");
        }

        AssessmentMetric metric = metricRepository.findById(request.getMetricId())
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found: " + request.getMetricId()));

        // Auto-score via benchmark lookup
        Integer score = calculateScoreFromBenchmark(metric, enrollment, request.getRawValue());

        AthleteMeasurement measurement = AthleteMeasurement.builder()
                .enrollment(enrollment)
                .metric(metric)
                .rawValue(request.getRawValue())
                .calculatedScore(score)
                .measuredDate(request.getMeasuredDate())
                .recordedBy(recordedBy)
                .build();

        AthleteMeasurement saved = measurementRepository.save(measurement);
        log.info("Measurement recorded: metric={}, enrollment={}, raw={}, score={}",
                metric.getMetricName(), enrollment.getId(), request.getRawValue(), score);
        return mapMeasurementToResponse(saved);
    }

    @Override
    public List<MeasurementResponse> recordBatchMeasurements(UUID campId, List<RecordMeasurementRequest> requests, String recordedBy) {
        return requests.stream()
                .map(req -> recordMeasurement(campId, req, recordedBy))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeasurementResponse> getMeasurementsByEnrollment(UUID enrollmentId) {
        return measurementRepository.findByEnrollmentId(enrollmentId).stream()
                .map(this::mapMeasurementToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getPhysicalScore(UUID enrollmentId) {
        Double avg = measurementRepository.calculateAverageScoreByEnrollmentId(enrollmentId);
        return avg != null ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    private Integer calculateScoreFromBenchmark(AssessmentMetric metric, CampEnrollment enrollment, BigDecimal rawValue) {
        AthleteProfile profile = athleteProfileRepository.findByUser_Id(enrollment.getUser().getId())
                .orElse(null);

        AgeCategory ageCategory = enrollment.getCamp().getAgeCategory();
        com.feadev.eaids.registrations.model.enums.Gender gender =
                profile != null ? profile.getGender() : null;

        List<MetricBenchmark> benchmarks = benchmarkRepository.findMatchingBenchmarks(
                metric.getId(), ageCategory, gender, rawValue);

        if (!benchmarks.isEmpty()) {
            return benchmarks.getFirst().getScore();
        }
        return null;
    }

    private MetricResponse mapMetricToResponse(AssessmentMetric metric) {
        return MetricResponse.builder()
                .id(metric.getId())
                .campId(metric.getCamp().getId())
                .category(metric.getCategory().name())
                .metricName(metric.getMetricName())
                .unit(metric.getUnit().name())
                .description(metric.getDescription())
                .isActive(metric.isActive())
                .lowerIsBetter(metric.isLowerIsBetter())
                .createdDate(metric.getCreatedDate())
                .build();
    }

    private BenchmarkResponse mapBenchmarkToResponse(MetricBenchmark benchmark) {
        return BenchmarkResponse.builder()
                .id(benchmark.getId())
                .metricId(benchmark.getMetric().getId())
                .minValue(benchmark.getMinValue())
                .maxValue(benchmark.getMaxValue())
                .score(benchmark.getScore())
                .ageCategory(benchmark.getAgeCategory() != null ? benchmark.getAgeCategory().name() : null)
                .gender(benchmark.getGender() != null ? benchmark.getGender().name() : null)
                .build();
    }

    private MeasurementResponse mapMeasurementToResponse(AthleteMeasurement m) {
        return MeasurementResponse.builder()
                .id(m.getId())
                .enrollmentId(m.getEnrollment().getId())
                .metricId(m.getMetric().getId())
                .metricName(m.getMetric().getMetricName())
                .category(m.getMetric().getCategory().name())
                .rawValue(m.getRawValue())
                .calculatedScore(m.getCalculatedScore())
                .measuredDate(m.getMeasuredDate())
                .recordedBy(m.getRecordedBy())
                .createdDate(m.getCreatedDate())
                .build();
    }
}
