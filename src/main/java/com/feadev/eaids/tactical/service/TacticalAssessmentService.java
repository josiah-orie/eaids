package com.feadev.eaids.tactical.service;

import com.feadev.eaids.tactical.dto.request.CreateTacticalMetricRequest;
import com.feadev.eaids.tactical.dto.request.RecordTacticalScoreRequest;
import com.feadev.eaids.tactical.dto.response.TacticalMetricResponse;
import com.feadev.eaids.tactical.dto.response.TacticalScoreResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TacticalAssessmentService {

    TacticalMetricResponse createMetric(UUID campId, CreateTacticalMetricRequest request);
    List<TacticalMetricResponse> getCampMetrics(UUID campId);
    void deleteMetric(UUID metricId);

    TacticalScoreResponse recordScore(UUID campId, RecordTacticalScoreRequest request, UUID evaluatorId);
    List<TacticalScoreResponse> getScoresByEnrollment(UUID enrollmentId);
    BigDecimal getWeightedTacticalScore(UUID enrollmentId);
}
