package com.feadev.eaids.positionprofile.service;

import com.feadev.eaids.positionprofile.dto.response.CampPositionProfileResponse;

import java.util.List;
import java.util.UUID;

public interface CampPositionProfileService {

    CampPositionProfileResponse assignProfile(UUID campId, UUID positionProfileId);

    List<CampPositionProfileResponse> getCampProfiles(UUID campId);

    void unassignProfile(UUID campId, UUID positionProfileId);
}
