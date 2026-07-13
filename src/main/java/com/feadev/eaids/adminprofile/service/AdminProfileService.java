package com.feadev.eaids.adminprofile.service;

import com.feadev.eaids.adminprofile.dto.request.CreateAdminProfileRequest;
import com.feadev.eaids.adminprofile.dto.request.UpdateAdminProfileRequest;
import com.feadev.eaids.adminprofile.dto.response.AdminProfileResponse;

import java.util.UUID;

public interface AdminProfileService {

    AdminProfileResponse createProfile(UUID userId, CreateAdminProfileRequest request);

    AdminProfileResponse updateProfile(UUID userId, UpdateAdminProfileRequest request);

    AdminProfileResponse getOwnProfile(UUID userId);

    AdminProfileResponse getProfileById(UUID profileId);
}
