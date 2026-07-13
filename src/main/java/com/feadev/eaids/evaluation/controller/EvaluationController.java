package com.feadev.eaids.evaluation.controller;

import com.feadev.eaids.evaluation.dto.request.AssignEvaluatorRequest;
import com.feadev.eaids.evaluation.dto.request.RecordEvaluationRequest;
import com.feadev.eaids.evaluation.dto.response.EvaluationResponse;
import com.feadev.eaids.evaluation.dto.response.EvaluatorResponse;
import com.feadev.eaids.evaluation.service.EvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/camps/{campId}/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping("/evaluators")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EvaluatorResponse> assignEvaluator(
            @PathVariable UUID campId,
            @Valid @RequestBody AssignEvaluatorRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evaluationService.assignEvaluator(campId, request));
    }

    @GetMapping("/evaluators")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<List<EvaluatorResponse>> getCampEvaluators(
            @PathVariable UUID campId) {
        return ResponseEntity.ok(evaluationService.getCampEvaluators(campId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('COACH', 'SCOUT')")
    public ResponseEntity<EvaluationResponse> recordEvaluation(
            @PathVariable UUID campId,
            @Valid @RequestBody RecordEvaluationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID evaluatorUserId = UUID.fromString(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(evaluationService.recordEvaluation(campId, request, evaluatorUserId));
    }

    @GetMapping("/enrollment/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<List<EvaluationResponse>> getEvaluationsByEnrollment(
            @PathVariable UUID campId, @PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(evaluationService.getEvaluationsByEnrollment(enrollmentId));
    }

    @GetMapping("/enrollment/{enrollmentId}/aggregated")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<BigDecimal> getAggregatedScore(
            @PathVariable UUID campId, @PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(evaluationService.getAggregatedCoachScore(enrollmentId));
    }
}
