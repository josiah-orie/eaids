package com.feadev.eaids.analytics.injuryrisk.service;

import com.feadev.eaids.analytics.injuryrisk.dto.HolisticRiskResponse;
import com.feadev.eaids.analytics.injuryrisk.dto.InjuryRiskResponse;

import java.util.List;
import java.util.UUID;

public interface InjuryRiskService {

    InjuryRiskResponse assessEnrollment(UUID enrollmentId);

    List<InjuryRiskResponse> batchAssessCamp(UUID campId);

    /**
     * Returns the full holistic risk profile for an athlete, combining the most
     * recent training-load assessment with a live medical-history evaluation.
     *
     * @param athleteUserId the athlete's user ID
     * @return a {@link HolisticRiskResponse}; gracefully defaults to LOW training
     *         risk when no prior enrollment assessment exists
     */
    HolisticRiskResponse getAthleteFullRiskProfile(UUID athleteUserId);
}
