package com.feadev.eaids.technical.service.impl;

import com.feadev.eaids.camp.model.Camp;
import com.feadev.eaids.camp.repository.CampRepository;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.technical.dto.request.CreateTechnicalMetricRequest;
import com.feadev.eaids.technical.dto.request.RecordTechnicalScoreRequest;
import com.feadev.eaids.technical.dto.response.TechnicalMetricResponse;
import com.feadev.eaids.technical.dto.response.TechnicalScoreResponse;
import com.feadev.eaids.technical.model.TechnicalMetric;
import com.feadev.eaids.technical.model.TechnicalScore;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import com.feadev.eaids.technical.repository.TechnicalMetricRepository;
import com.feadev.eaids.technical.repository.TechnicalScoreRepository;
import com.feadev.eaids.technical.service.TechnicalAssessmentService;
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
public class TechnicalAssessmentServiceImpl implements TechnicalAssessmentService {

    private final TechnicalMetricRepository metricRepository;
    private final TechnicalScoreRepository scoreRepository;
    private final CampRepository campRepository;
    private final CampEnrollmentRepository enrollmentRepository;

    @Override
    public TechnicalMetricResponse createMetric(UUID campId, CreateTechnicalMetricRequest request) {
        Camp camp = campRepository.findById(campId)
                .orElseThrow(() -> new ResourceNotFoundException("Camp not found: " + campId));

        TechnicalMetric metric = TechnicalMetric.builder()
                .camp(camp)
                .position(request.getPosition())
                .metricName(request.getMetricName())
                .maxScore(request.getMaxScore())
                .weight(request.getWeight())
                .build();

        TechnicalMetric saved = metricRepository.save(metric);
        log.info("Technical metric '{}' created for camp {} position {}",
                saved.getMetricName(), campId, request.getPosition());
        return mapMetricToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TechnicalMetricResponse> getCampMetrics(UUID campId, FootballPosition position) {
        List<TechnicalMetric> metrics;
        if (position != null) {
            metrics = metricRepository.findByCampIdAndPosition(campId, position);
        } else {
            metrics = metricRepository.findByCampId(campId);
        }
        return metrics.stream().map(this::mapMetricToResponse).collect(Collectors.toList());
    }

    @Override
    public void deleteMetric(UUID metricId) {
        TechnicalMetric metric = metricRepository.findById(metricId)
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found: " + metricId));
        metricRepository.delete(metric);
    }

    @Override
    public TechnicalScoreResponse recordScore(UUID campId, RecordTechnicalScoreRequest request, UUID evaluatorId) {
        CampEnrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + request.getEnrollmentId()));

        if (!enrollment.getCamp().getId().equals(campId)) {
            throw new IllegalStateException("Enrollment does not belong to this camp");
        }

        TechnicalMetric metric = metricRepository.findById(request.getMetricId())
                .orElseThrow(() -> new ResourceNotFoundException("Metric not found: " + request.getMetricId()));

        if (request.getScore() > metric.getMaxScore()) {
            throw new IllegalStateException("Score cannot exceed max score of " + metric.getMaxScore());
        }

        TechnicalScore score = TechnicalScore.builder()
                .enrollment(enrollment)
                .metric(metric)
                .score(request.getScore())
                .evaluatorId(evaluatorId)
                .build();

        TechnicalScore saved = scoreRepository.save(score);
        log.info("Technical score recorded: metric={}, enrollment={}, score={}",
                metric.getMetricName(), enrollment.getId(), request.getScore());
        return mapScoreToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TechnicalScoreResponse> getScoresByEnrollment(UUID enrollmentId) {
        return scoreRepository.findByEnrollmentId(enrollmentId).stream()
                .map(this::mapScoreToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getWeightedTechnicalScore(UUID enrollmentId, FootballPosition position) {
        List<TechnicalScore> scores = scoreRepository.findByEnrollmentIdAndMetric_Position(enrollmentId, position);
        if (scores.isEmpty()) return BigDecimal.ZERO;

        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (TechnicalScore ts : scores) {
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

    private TechnicalMetricResponse mapMetricToResponse(TechnicalMetric metric) {
        return TechnicalMetricResponse.builder()
                .id(metric.getId())
                .campId(metric.getCamp().getId())
                .position(metric.getPosition().name())
                .metricName(metric.getMetricName())
                .maxScore(metric.getMaxScore())
                .weight(metric.getWeight())
                .createdDate(metric.getCreatedDate())
                .build();
    }

    private TechnicalScoreResponse mapScoreToResponse(TechnicalScore score) {
        return TechnicalScoreResponse.builder()
                .id(score.getId())
                .enrollmentId(score.getEnrollment().getId())
                .metricId(score.getMetric().getId())
                .metricName(score.getMetric().getMetricName())
                .position(score.getMetric().getPosition().name())
                .score(score.getScore())
                .maxScore(score.getMetric().getMaxScore())
                .evaluatorId(score.getEvaluatorId())
                .createdDate(score.getCreatedDate())
                .build();
    }
}
