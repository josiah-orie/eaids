package com.feadev.eaids.analytics.prediction;

import com.feadev.eaids.analytics.prediction.dto.PotentialPredictionResponse;

import java.util.List;
import java.util.UUID;

/**
 * Interface for athlete potential prediction engines.
 * Designed for future extensibility — implementations can be swapped
 * (e.g., rule-based now, ML-based later) without changing the contract.
 */
public interface PotentialPredictionEngine {

    /**
     * Get the latest prediction for an athlete.
     *
     * @param athleteUserId the athlete's user ID
     * @return the most recent prediction, or an "insufficient data" response
     */
    PotentialPredictionResponse getPrediction(UUID athleteUserId);

    /**
     * Generate predictions for all eligible athletes (batch).
     *
     * @return list of generated predictions
     */
    List<PotentialPredictionResponse> generateBatchPredictions();
}
