package com.feadev.eaids.registrations.service.impl;

import com.feadev.eaids.registrations.dto.request.CoachProfileUpdateRequest;
import com.feadev.eaids.registrations.dto.response.CoachProfileResponse;
import com.feadev.eaids.registrations.exception.AccountNotActiveException;
import com.feadev.eaids.registrations.exception.MaxLimitReachedException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.CoachDocument;
import com.feadev.eaids.registrations.model.CoachProfile;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.DocumentType;
import com.feadev.eaids.registrations.model.enums.UserStatus;
import com.feadev.eaids.registrations.model.enums.VerificationStatus;
import com.feadev.eaids.registrations.repository.CoachDocumentRepository;
import com.feadev.eaids.registrations.repository.CoachProfileRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.registrations.service.CoachService;
import com.feadev.eaids.registrations.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CoachServiceImpl implements CoachService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_DOCUMENT_COUNT = 20;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png"
    );

    private final UserRepository userRepository;
    private final CoachProfileRepository coachProfileRepository;
    private final CoachDocumentRepository coachDocumentRepository;
    private final StorageService storageService;
    private final AuditService auditService;

    @Override
    @Transactional(readOnly = true)
    public CoachProfileResponse getProfile(UUID userId) {
        CoachProfile profile = coachProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach profile not found"));
        return mapToResponse(profile);
    }

    @Override
    public CoachProfileResponse updateProfile(UUID userId, CoachProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("Only active coaches may manage their profile");
        }

        CoachProfile profile = coachProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach profile not found"));

        // Update only non-null fields
        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getNationality() != null) {
            profile.setNationality(request.getNationality());
        }
        if (request.getStateOfResidence() != null) {
            profile.setStateOfResidence(request.getStateOfResidence());
        }
        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getEmailAddress() != null) {
            profile.setEmailAddress(request.getEmailAddress());
        }
        if (request.getCurrentSport() != null) {
            profile.setCurrentSport(request.getCurrentSport());
        }
        if (request.getCurrentOrganization() != null) {
            profile.setCurrentOrganization(request.getCurrentOrganization());
        }
        if (request.getCurrentPosition() != null) {
            profile.setCurrentPosition(request.getCurrentPosition());
        }
        if (request.getYearsOfExperience() != null) {
            profile.setYearsOfExperience(request.getYearsOfExperience());
        }
        if (request.getAreaOfInterest() != null) {
            profile.setAreaOfInterest(request.getAreaOfInterest());
        }
        if (request.getAvailability() != null) {
            profile.setAvailability(request.getAvailability());
        }
        if (request.getAmericanFootballExperience() != null) {
            profile.setAmericanFootballExperience(request.getAmericanFootballExperience());
        }
        if (request.getInvolvementLevel() != null) {
            profile.setInvolvementLevel(request.getInvolvementLevel());
        }
        if (request.getCoachingInterests() != null) {
            if (request.getCoachingInterests().isEmpty()) {
                throw new IllegalArgumentException("At least one coaching interest is required");
            }
            profile.getCoachingInterests().clear();
            profile.getCoachingInterests().addAll(request.getCoachingInterests());
        }

        CoachProfile savedProfile = coachProfileRepository.save(profile);
        log.info("Coach profile updated for user {}", userId);
        return mapToResponse(savedProfile);
    }

    @Override
    public CoachProfileResponse.DocumentResponse uploadDocument(UUID userId, MultipartFile file, DocumentType type) {
        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "Invalid file type. Allowed types: PDF, JPG, JPEG, PNG");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "File size exceeds the maximum allowed size of 10MB");
        }

        CoachProfile profile = coachProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach profile not found"));

        // Check document count limit
        int documentCount = coachDocumentRepository.countByCoachProfileId(profile.getId());
        if (documentCount >= MAX_DOCUMENT_COUNT) {
            throw new MaxLimitReachedException(
                    "Maximum document limit of 20 has been reached");
        }

        // Store file via StorageService
        String folder = "coaches/" + userId + "/certifications";
        String fileUrl = storageService.store(file, folder);

        // Create CoachDocument record
        CoachDocument document = CoachDocument.builder()
                .coachProfile(profile)
                .documentType(type)
                .fileName(file.getOriginalFilename())
                .fileUrl(fileUrl)
                .fileSize(file.getSize())
                .verificationStatus(VerificationStatus.PENDING)
                .uploadedAt(LocalDateTime.now())
                .build();

        CoachDocument savedDocument = coachDocumentRepository.save(document);
        log.info("Document uploaded for coach user {}: type={}, fileName={}", userId, type, file.getOriginalFilename());

        return mapToDocumentResponse(savedDocument);
    }

    private CoachProfileResponse mapToResponse(CoachProfile profile) {
        List<CoachProfileResponse.DocumentResponse> documentResponses = profile.getDocuments().stream()
                .map(this::mapToDocumentResponse)
                .collect(Collectors.toList());

        return CoachProfileResponse.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .gender(profile.getGender().name())
                .dateOfBirth(profile.getDateOfBirth())
                .nationality(profile.getNationality())
                .stateOfResidence(profile.getStateOfResidence())
                .phoneNumber(profile.getPhoneNumber())
                .emailAddress(profile.getEmailAddress())
                .currentSport(profile.getCurrentSport())
                .currentOrganization(profile.getCurrentOrganization())
                .currentPosition(profile.getCurrentPosition())
                .yearsOfExperience(profile.getYearsOfExperience() != null
                        ? profile.getYearsOfExperience().toString() : null)
                .areaOfInterest(profile.getAreaOfInterest().name())
                .availability(profile.getAvailability().name())
                .americanFootballExperience(profile.getAmericanFootballExperience().name())
                .involvementLevel(profile.getInvolvementLevel().name())
                .coachingInterests(profile.getCoachingInterests().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet()))
                .documents(documentResponses)
                .build();
    }

    private CoachProfileResponse.DocumentResponse mapToDocumentResponse(CoachDocument document) {
        return CoachProfileResponse.DocumentResponse.builder()
                .id(document.getId())
                .documentType(document.getDocumentType().name())
                .fileName(document.getFileName())
                .fileSize(document.getFileSize())
                .verificationStatus(document.getVerificationStatus().name())
                .uploadedAt(document.getUploadedAt())
                .build();
    }
}
