package com.feadev.eaids.technical.service;

import com.feadev.eaids.technical.dto.request.CreateTechnicalMetricRequest;
import com.feadev.eaids.technical.dto.request.RecordTechnicalScoreRequest;
import com.feadev.eaids.technical.dto.response.TechnicalMetricResponse;
import com.feadev.eaids.technical.dto.response.TechnicalScoreResponse;
import com.feadev.eaids.technical.model.enums.FootballPosition;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TechnicalAssessmentService {

    TechnicalMetricResponse createMetric(UUID campId, CreateTechnicalMetricRequest request);
    List<TechnicalMetricResponse> getCampMetrics(UUID campId, FootballPosition position);
    void deleteMetric(UUID metricId);

    TechnicalScoreResponse recordScore(UUID campId, RecordTechnicalScoreRequest request, UUID evaluatorId);
    List<TechnicalScoreResponse> getScoresByEnrollment(UUID enrollmentId);
    BigDecimal getWeightedTechnicalScore(UUID enrollmentId, FootballPosition position);
}
