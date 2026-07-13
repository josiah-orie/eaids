package com.feadev.eaids.analytics.recommendation.controller;

import com.feadev.eaids.analytics.recommendation.PositionRecommendationEngine;
import com.feadev.eaids.analytics.recommendation.dto.PositionRecommendationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics/recommendations")
@RequiredArgsConstructor
public class PositionRecommendationController {

    private final PositionRecommendationEngine recommendationEngine;

    @GetMapping("/{enrollmentId}/position")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<PositionRecommendationResponse> getPositionRecommendation(
            @PathVariable UUID enrollmentId) {
        PositionRecommendationResponse response = recommendationEngine.recommend(enrollmentId);
        return ResponseEntity.ok(response);
    }
}
