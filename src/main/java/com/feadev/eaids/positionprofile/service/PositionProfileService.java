package com.feadev.eaids.positionprofile.service;

import com.feadev.eaids.positionprofile.dto.request.CreatePositionProfileRequest;
import com.feadev.eaids.positionprofile.dto.request.UpdatePositionProfileRequest;
import com.feadev.eaids.positionprofile.dto.response.PositionProfileResponse;
import com.feadev.eaids.technical.model.enums.FootballPosition;

import java.util.List;
import java.util.UUID;

public interface PositionProfileService {

    PositionProfileResponse createProfile(CreatePositionProfileRequest request);

    PositionProfileResponse getProfile(UUID profileId);

    List<PositionProfileResponse> getAllProfiles(FootballPosition position, Boolean active);

    PositionProfileResponse updateProfile(UUID profileId, UpdatePositionProfileRequest request);

    void deleteProfile(UUID profileId);
}
