package com.feadev.eaids.medical.service;

import com.feadev.eaids.medical.dto.request.UpdateMedicalProfileRequest;
import com.feadev.eaids.medical.dto.response.MedicalProfileResponse;
import com.feadev.eaids.registrations.model.enums.UserRole;

import java.util.UUID;

public interface MedicalProfileService {

    /**
     * Creates or updates the medical profile for the given athlete.
     * Athletes may only update their own profile.
     * Medical officers and admins may update any profile.
     */
    MedicalProfileResponse createOrUpdateProfile(UUID athleteUserId,
                                                  UpdateMedicalProfileRequest request,
                                                  UUID actorId,
                                                  UserRole actorRole);

    /**
     * Retrieves the medical profile for the given athlete.
     * Athletes may only view their own profile.
     * Medical officers, physiotherapists, and admins may view any profile.
     */
    MedicalProfileResponse getProfile(UUID athleteUserId, UUID actorId, UserRole actorRole);
}
