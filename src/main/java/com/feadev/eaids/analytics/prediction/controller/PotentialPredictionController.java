package com.feadev.eaids.analytics.prediction.controller;

import com.feadev.eaids.analytics.prediction.PotentialPredictionEngine;
import com.feadev.eaids.analytics.prediction.dto.PotentialPredictionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics/predictions")
@RequiredArgsConstructor
public class PotentialPredictionController {

    private final PotentialPredictionEngine predictionEngine;

    @GetMapping("/{athleteUserId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<PotentialPredictionResponse> getPrediction(@PathVariable UUID athleteUserId) {
        PotentialPredictionResponse response = predictionEngine.getPrediction(athleteUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PotentialPredictionResponse>> generateBatchPredictions() {
        List<PotentialPredictionResponse> responses = predictionEngine.generateBatchPredictions();
        return ResponseEntity.ok(responses);
    }
}
