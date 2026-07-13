package com.feadev.eaids.evaluation.service.impl;

import com.feadev.eaids.camp.model.Camp;
import com.feadev.eaids.camp.model.enums.AggregationStrategy;
import com.feadev.eaids.camp.repository.CampRepository;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.evaluation.dto.request.AssignEvaluatorRequest;
import com.feadev.eaids.evaluation.dto.request.RecordEvaluationRequest;
import com.feadev.eaids.evaluation.dto.response.EvaluationResponse;
import com.feadev.eaids.evaluation.dto.response.EvaluatorResponse;
import com.feadev.eaids.evaluation.model.CampEvaluator;
import com.feadev.eaids.evaluation.model.CoachEvaluation;
import com.feadev.eaids.evaluation.repository.CampEvaluatorRepository;
import com.feadev.eaids.evaluation.repository.CoachEvaluationRepository;
import com.feadev.eaids.evaluation.service.EvaluationService;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EvaluationServiceImpl implements EvaluationService {

    private final CampEvaluatorRepository evaluatorRepository;
    private final CoachEvaluationRepository evaluationRepository;
    private final CampRepository campRepository;
    private final CampEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Override
    public EvaluatorResponse assignEvaluator(UUID campId, AssignEvaluatorRequest request) {
        Camp camp = campRepository.findById(campId)
                .orElseThrow(() -> new ResourceNotFoundException("Camp not found: " + campId));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUserId()));

        if (evaluatorRepository.existsByCampIdAndUserId(campId, request.getUserId())) {
            throw new IllegalStateException("User is already assigned as evaluator for this camp");
        }

        CampEvaluator evaluator = CampEvaluator.builder()
                .camp(camp)
                .user(user)
                .role(request.getRole())
                .isLead(request.isLead())
                .build();

        CampEvaluator saved = evaluatorRepository.save(evaluator);
        log.info("Evaluator {} assigned to camp {}", request.getUserId(), campId);
        return mapEvaluatorToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluatorResponse> getCampEvaluators(UUID campId) {
        return evaluatorRepository.findByCampId(campId).stream()
                .map(this::mapEvaluatorToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EvaluationResponse recordEvaluation(UUID campId, RecordEvaluationRequest request, UUID evaluatorUserId) {
        CampEvaluator evaluator = evaluatorRepository.findByCampIdAndUserId(campId, evaluatorUserId)
                .orElseThrow(() -> new IllegalStateException("User is not an evaluator for this camp"));

        CampEnrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + request.getEnrollmentId()));

        if (!enrollment.getCamp().getId().equals(campId)) {
            throw new IllegalStateException("Enrollment does not belong to this camp");
        }

        CoachEvaluation evaluation = CoachEvaluation.builder()
                .enrollment(enrollment)
                .evaluator(evaluator)
                .technicalScore(request.getTechnicalScore())
                .tacticalScore(request.getTacticalScore())
                .leadershipScore(request.getLeadershipScore())
                .characterScore(request.getCharacterScore())
                .recommendation(request.getRecommendation())
                .comments(request.getComments())
                .build();

        CoachEvaluation saved = evaluationRepository.save(evaluation);
        log.info("Evaluation recorded for enrollment {} by evaluator {}", request.getEnrollmentId(), evaluatorUserId);
        return mapEvaluationToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EvaluationResponse> getEvaluationsByEnrollment(UUID enrollmentId) {
        return evaluationRepository.findByEnrollmentId(enrollmentId).stream()
                .map(this::mapEvaluationToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getAggregatedCoachScore(UUID enrollmentId) {
        List<CoachEvaluation> evaluations = evaluationRepository.findByEnrollmentId(enrollmentId);
        if (evaluations.isEmpty()) return BigDecimal.ZERO;

        CampEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + enrollmentId));
        AggregationStrategy strategy = enrollment.getCamp().getAggregationStrategy();

        List<BigDecimal> scores = evaluations.stream()
                .map(e -> {
                    BigDecimal sum = BigDecimal.ZERO;
                    int count = 0;
                    if (e.getTechnicalScore() != null) { sum = sum.add(e.getTechnicalScore()); count++; }
                    if (e.getTacticalScore() != null) { sum = sum.add(e.getTacticalScore()); count++; }
                    if (e.getLeadershipScore() != null) { sum = sum.add(e.getLeadershipScore()); count++; }
                    if (e.getCharacterScore() != null) { sum = sum.add(e.getCharacterScore()); count++; }
                    return count > 0 ? sum.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
                })
                .collect(Collectors.toList());

        return switch (strategy) {
            case AVERAGE -> scores.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
            case MEDIAN -> {
                List<BigDecimal> sorted = scores.stream().sorted().collect(Collectors.toList());
                int size = sorted.size();
                yield size % 2 == 0
                        ? sorted.get(size / 2 - 1).add(sorted.get(size / 2)).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP)
                        : sorted.get(size / 2);
            }
            case WEIGHTED_AVERAGE -> scores.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
        };
    }

    private EvaluatorResponse mapEvaluatorToResponse(CampEvaluator evaluator) {
        return EvaluatorResponse.builder()
                .id(evaluator.getId())
                .campId(evaluator.getCamp().getId())
                .userId(evaluator.getUser().getId())
                .role(evaluator.getRole().name())
                .isLead(evaluator.isLead())
                .createdDate(evaluator.getCreatedDate())
                .build();
    }

    private EvaluationResponse mapEvaluationToResponse(CoachEvaluation eval) {
        BigDecimal avg = BigDecimal.ZERO;
        int count = 0;
        if (eval.getTechnicalScore() != null) { avg = avg.add(eval.getTechnicalScore()); count++; }
        if (eval.getTacticalScore() != null) { avg = avg.add(eval.getTacticalScore()); count++; }
        if (eval.getLeadershipScore() != null) { avg = avg.add(eval.getLeadershipScore()); count++; }
        if (eval.getCharacterScore() != null) { avg = avg.add(eval.getCharacterScore()); count++; }
        BigDecimal averageScore = count > 0 ? avg.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        return EvaluationResponse.builder()
                .id(eval.getId())
                .enrollmentId(eval.getEnrollment().getId())
                .evaluatorId(eval.getEvaluator().getId())
                .technicalScore(eval.getTechnicalScore())
                .tacticalScore(eval.getTacticalScore())
                .leadershipScore(eval.getLeadershipScore())
                .characterScore(eval.getCharacterScore())
                .averageScore(averageScore)
                .recommendation(eval.getRecommendation())
                .comments(eval.getComments())
                .createdDate(eval.getCreatedDate())
                .build();
    }
}
