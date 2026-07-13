package com.feadev.eaids.registrations.service;

import com.feadev.eaids.registrations.dto.request.CoachProfileUpdateRequest;
import com.feadev.eaids.registrations.dto.response.CoachProfileResponse;
import com.feadev.eaids.registrations.model.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface CoachService {

    CoachProfileResponse getProfile(UUID userId);

    CoachProfileResponse updateProfile(UUID userId, CoachProfileUpdateRequest request);

    CoachProfileResponse.DocumentResponse uploadDocument(UUID userId, MultipartFile file, DocumentType type);
}
