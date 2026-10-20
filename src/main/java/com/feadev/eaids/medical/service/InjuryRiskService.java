package com.feadev.eaids.medical.service;

import com.feadev.eaids.medical.dto.response.InjuryRiskResponse;

import java.util.UUID;

public interface InjuryRiskService {

    /**
     * Returns the cached injury risk profile for an athlete.
     * Computed on first call; served from cache on subsequent calls until evicted.
     *
     * @param athleteUserId the athlete's user ID
     * @return the risk profile, never {@code null}
     */
    InjuryRiskResponse getRiskProfile(UUID athleteUserId);

    /**
     * Evicts the cached risk profile for an athlete.
     * Called automatically after any injury or medical assessment change.
     *
     * @param athleteUserId the athlete whose cache entry should be cleared
     */
    void evictRiskCache(UUID athleteUserId);
}
