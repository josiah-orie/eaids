package com.feadev.eaids.analytics.recommendation;

import com.feadev.eaids.analytics.recommendation.dto.PositionRecommendationResponse;

import java.util.UUID;

/**
 * Interface for position recommendation engines.
 * Designed for future extensibility — implementations can be swapped
 * (e.g., rule-based now, ML-based later) without changing the contract.
 */
public interface PositionRecommendationEngine {

    /**
     * Generate a position recommendation for the given enrollment.
     *
     * @param enrollmentId the camp enrollment ID to analyze
     * @return recommendation with best, secondary, and potential positions
     */
    PositionRecommendationResponse recommend(UUID enrollmentId);
}
