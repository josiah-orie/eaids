package com.feadev.eaids.medical.service;

import com.feadev.eaids.medical.dto.request.IssueClearanceRequest;
import com.feadev.eaids.medical.dto.request.RevokeClearanceRequest;
import com.feadev.eaids.medical.dto.response.MedicalClearanceResponse;
import com.feadev.eaids.medical.dto.response.ParticipationEligibilityResponse;
import com.feadev.eaids.registrations.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MedicalClearanceService {

    MedicalClearanceResponse issueClearance(UUID athleteUserId,
                                             IssueClearanceRequest request,
                                             UUID actorId,
                                             UserRole actorRole);

    MedicalClearanceResponse revokeClearance(UUID clearanceId,
                                              RevokeClearanceRequest request,
                                              UUID actorId,
                                              UserRole actorRole);

    /**
     * Full response for MO/admin/athlete (own).
     * Operational-only {@link ParticipationEligibilityResponse} for coach/scout.
     */
    Object getClearance(UUID clearanceId, UUID actorId, UserRole actorRole);

    Page<MedicalClearanceResponse> getClearancesForAthlete(UUID athleteUserId,
                                                            UUID actorId,
                                                            UserRole actorRole,
                                                            Pageable pageable);

    /**
     * Returns the participation eligibility for an athlete.
     * Safe for any authenticated caller — coaches and scouts receive restricted view.
     */
    ParticipationEligibilityResponse getParticipationEligibility(UUID athleteUserId,
                                                                   UUID actorId,
                                                                   UserRole actorRole);
}
