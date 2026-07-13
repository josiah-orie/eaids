package com.feadev.eaids.registrations.service;

import com.feadev.eaids.registrations.dto.request.AthleteProfileCompletionRequest;
import com.feadev.eaids.registrations.dto.request.AthleteProfileUpdateRequest;
import com.feadev.eaids.registrations.dto.request.GuardianConsentRequest;
import com.feadev.eaids.registrations.dto.request.VideoAddRequest;
import com.feadev.eaids.registrations.dto.response.AthleteProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface AthleteService {

    AthleteProfileResponse completeProfile(UUID userId, AthleteProfileCompletionRequest request);

    AthleteProfileResponse submitGuardianConsent(UUID userId, GuardianConsentRequest request);

    AthleteProfileResponse getProfile(UUID userId);

    AthleteProfileResponse updateProfile(UUID userId, AthleteProfileUpdateRequest request);

    AthleteProfileResponse.VideoResponse addVideo(UUID userId, VideoAddRequest request);

    void deleteVideo(UUID userId, UUID videoId);

    AthleteProfileResponse uploadProfileImage(UUID userId, MultipartFile file);

    AthleteProfileResponse uploadIconImage(UUID userId, MultipartFile file);
}
