package com.feadev.eaids.tactical.service.impl;

import com.feadev.eaids.camp.model.Camp;
import com.feadev.eaids.camp.repository.CampRepository;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.tactical.dto.request.CreateTacticalMetricRequest;
import com.feadev.eaids.tactical.dto.request.RecordTacticalScoreRequest;
import com.feadev.eaids.tactical.dto.response.TacticalMetricResponse;
import com.feadev.eaids.tactical.dto.response.TacticalScoreResponse;
import com.feadev.eaids.tactical.model.TacticalMetric;
import com.feadev.eaids.tactical.model.TacticalScore;
import com.feadev.eaids.tactical.repository.TacticalMetricRepository;
import com.feadev.eaids.tactical.repository.TacticalScoreRepository;
import com.feadev.eaids.tactical.service.TacticalAssessmentService;
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
public class TacticalAssessmentServiceImpl implements TacticalAssessmentService {

    private final TacticalMetricRepository metricRepository;
    private final TacticalScoreRepository scoreRepository;
    private final CampRepository campRepository;
    private final CampEnrollmentRepository enrollmentRepository;

    @Override
    public TacticalMetricResponse createMetric(UUID campId, CreateTacticalMetricRequest request) {
        Camp camp = campRepository.findById(campId)
                .orElseThrow(() -> new ResourceNotFoundException("Camp not found: " + campId));

        TacticalMetric metric = TacticalMetric.builder()
                .camp(camp)
                .metricName(request.getMetricName())
                .maxScore(request.getMaxScore())
                .weight(request.getWeight())
                .description(request.getDescription())
                .build();

        TacticalMetric saved = metricRepository.save(metric);
        log.info("Tactical metric '{}' created for camp {}", saved.getMetricName(), campId);
        return mapMetricToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TacticalMetricResponse> getCampMetrics(UUID campId) {
        return metricRepository.findByCampId(campId).stream()
                .map(this::mapMetricToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMetric(UUID metricId) {
        TacticalMetric metric = metricRepository.findById(metricId)
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found: " + metricId));
        metricRepository.delete(metric);
    }

    @Override
    public TacticalScoreResponse recordScore(UUID campId, RecordTacticalScoreRequest request, UUID evaluatorId) {
        CampEnrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + request.getEnrollmentId()));

        if (!enrollment.getCamp().getId().equals(campId)) {
            throw new IllegalStateException("Enrollment does not belong to this camp");
        }

        TacticalMetric metric = metricRepository.findById(request.getMetricId())
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found: " + request.getMetricId()));

        if (request.getScore() > metric.getMaxScore()) {
            throw new IllegalStateException("Score cannot exceed max score of " + metric.getMaxScore());
        }

        TacticalScore score = TacticalScore.builder()
                .enrollment(enrollment)
                .metric(metric)
                .score(request.getScore())
                .evaluatorId(evaluatorId)
                .build();

        TacticalScore saved = scoreRepository.save(score);
        return mapScoreToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TacticalScoreResponse> getScoresByEnrollment(UUID enrollmentId) {
        return scoreRepository.findByEnrollmentId(enrollmentId).stream()
                .map(this::mapScoreToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getWeightedTacticalScore(UUID enrollmentId) {
        List<TacticalScore> scores = scoreRepository.findByEnrollmentId(enrollmentId);
        if (scores.isEmpty()) return BigDecimal.ZERO;

        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (TacticalScore ts : scores) {
            BigDecimal weight = ts.getMetric().getWeight() != null ? ts.getMetric().getWeight() : BigDecimal.ONE;
            BigDecimal normalized = BigDecimal.valueOf(ts.getScore())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(ts.getMetric().getMaxScore()), 2, RoundingMode.HALF_UP);
            totalWeightedScore = totalWeightedScore.add(normalized.multiply(weight));
            totalWeight = totalWeight.add(weight);
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return totalWeightedScore.divide(totalWeight, 2, RoundingMode.HALF_UP);
    }

    private TacticalMetricResponse mapMetricToResponse(TacticalMetric metric) {
        return TacticalMetricResponse.builder()
                .id(metric.getId())
                .campId(metric.getCamp().getId())
                .metricName(metric.getMetricName())
                .maxScore(metric.getMaxScore())
                .weight(metric.getWeight())
                .description(metric.getDescription())
                .createdDate(metric.getCreatedDate())
                .build();
    }

    private TacticalScoreResponse mapScoreToResponse(TacticalScore score) {
        return TacticalScoreResponse.builder()
                .id(score.getId())
                .enrollmentId(score.getEnrollment().getId())
                .metricId(score.getMetric().getId())
                .metricName(score.getMetric().getMetricName())
                .score(score.getScore())
                .maxScore(score.getMetric().getMaxScore())
                .evaluatorId(score.getEvaluatorId())
                .createdDate(score.getCreatedDate())
                .build();
    }
}
