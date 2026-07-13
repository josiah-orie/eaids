package com.feadev.eaids.evaluation.service;

import com.feadev.eaids.evaluation.dto.request.AssignEvaluatorRequest;
import com.feadev.eaids.evaluation.dto.request.RecordEvaluationRequest;
import com.feadev.eaids.evaluation.dto.response.EvaluationResponse;
import com.feadev.eaids.evaluation.dto.response.EvaluatorResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface EvaluationService {

    EvaluatorResponse assignEvaluator(UUID campId, AssignEvaluatorRequest request);
    List<EvaluatorResponse> getCampEvaluators(UUID campId);

    EvaluationResponse recordEvaluation(UUID campId, RecordEvaluationRequest request, UUID evaluatorUserId);
    List<EvaluationResponse> getEvaluationsByEnrollment(UUID enrollmentId);
    BigDecimal getAggregatedCoachScore(UUID enrollmentId);
}
