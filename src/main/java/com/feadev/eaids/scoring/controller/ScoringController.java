package com.feadev.eaids.scoring.controller;

import com.feadev.eaids.scoring.dto.request.CreateWeightProfileRequest;
import com.feadev.eaids.scoring.dto.response.AthleteScoreResponse;
import com.feadev.eaids.scoring.dto.response.WeightProfileResponse;
import com.feadev.eaids.scoring.service.ScoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/camps/{campId}/scoring")
@RequiredArgsConstructor
public class ScoringController {

    private final ScoringService scoringService;

    @PostMapping("/weight-profiles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WeightProfileResponse> createWeightProfile(
            @PathVariable UUID campId,
            @Valid @RequestBody CreateWeightProfileRequest request) {
        WeightProfileResponse response = scoringService.createWeightProfile(campId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/weight-profiles")
    public ResponseEntity<List<WeightProfileResponse>> getWeightProfiles(@PathVariable UUID campId) {
        return ResponseEntity.ok(scoringService.getWeightProfiles(campId));
    }

    @PostMapping("/calculate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AthleteScoreResponse>> calculateScores(@PathVariable UUID campId) {
        List<AthleteScoreResponse> responses = scoringService.calculateScores(campId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/scores/enrollment/{enrollmentId}")
    public ResponseEntity<AthleteScoreResponse> getAthleteScore(
            @PathVariable UUID campId,
            @PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(scoringService.getAthleteScore(enrollmentId));
    }
}
