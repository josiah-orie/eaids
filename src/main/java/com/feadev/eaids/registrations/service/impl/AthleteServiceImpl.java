package com.feadev.eaids.registrations.service.impl;

import com.feadev.eaids.registrations.dto.request.AthleteProfileCompletionRequest;
import com.feadev.eaids.registrations.dto.request.AthleteProfileUpdateRequest;
import com.feadev.eaids.registrations.dto.request.GuardianConsentRequest;
import com.feadev.eaids.registrations.dto.request.VideoAddRequest;
import com.feadev.eaids.registrations.dto.response.AthleteProfileResponse;
import com.feadev.eaids.registrations.event.GuardianConsentReceivedEvent;
import com.feadev.eaids.registrations.exception.AccountNotActiveException;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.exception.MaxLimitReachedException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.AthleteProfile;
import com.feadev.eaids.registrations.model.AthleteVideo;
import com.feadev.eaids.registrations.model.GuardianConsent;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.UserStatus;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.registrations.repository.AthleteVideoRepository;
import com.feadev.eaids.registrations.repository.GuardianConsentRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AthleteService;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.registrations.service.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AthleteServiceImpl implements AthleteService {

    private final UserRepository userRepository;
    private final AthleteProfileRepository athleteProfileRepository;
    private final AthleteVideoRepository athleteVideoRepository;
    private final GuardianConsentRepository guardianConsentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;
    private final StorageService storageService;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of("image/jpeg", "image/jpg", "image/png");
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB

    @Override
    public AthleteProfileResponse completeProfile(UUID userId, AthleteProfileCompletionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isEmailVerified() || user.getStatus() != UserStatus.PENDING_VERIFICATION) {
            throw new InvalidStateTransitionException(
                    "Email must be verified before profile completion");
        }

        int age = Period.between(request.getDateOfBirth(), LocalDate.now()).getYears();

        AthleteProfile profile = AthleteProfile.builder()
                .user(user)
                .fullName(request.getFullName())
                .age(age)
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .stateOfResidence(request.getStateOfResidence())
                .countryOfResidence(request.getCountryOfResidence())
                .nationality(request.getNationality())
                .heightCm(request.getHeightCm())
                .weightKg(request.getWeightKg())
                .athleteInterest(request.getAthleteInterest())
                .disabilityStatus(request.isDisabilityStatus())
                .sportsBackgrounds(request.getSportsBackgrounds())
                .videos(new ArrayList<>())
                .build();

        AthleteProfile savedProfile = athleteProfileRepository.save(profile);

        // Store videos as AthleteVideo entities
        List<AthleteVideo> videos = request.getVideos().stream()
                .map(videoReq -> AthleteVideo.builder()
                        .athleteProfile(savedProfile)
                        .url(videoReq.getUrl())
                        .videoType(videoReq.getVideoType())
                        .build())
                .collect(Collectors.toList());
        List<AthleteVideo> savedVideos = athleteVideoRepository.saveAll(videos);
        savedProfile.setVideos(savedVideos);

        // Determine status based on age
        if (age < 18) {
            user.setStatus(UserStatus.PENDING_CONSENT);
        } else {
            user.setStatus(UserStatus.UNDER_REVIEW);
        }
        userRepository.save(user);

        log.info("Profile completed for user {} with status {}", userId, user.getStatus());
        return mapToResponse(savedProfile);
    }

    @Override
    public AthleteProfileResponse submitGuardianConsent(UUID userId, GuardianConsentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.PENDING_CONSENT) {
            throw new InvalidStateTransitionException(
                    "Guardian consent is not applicable for the current user status");
        }

        if (!request.getConsentAccepted()) {
            throw new InvalidStateTransitionException("Consent must be accepted");
        }

        if (user.getEmail().equalsIgnoreCase(request.getGuardianEmail())) {
            throw new InvalidStateTransitionException(
                    "Guardian email must differ from the athlete's registered email");
        }

        GuardianConsent consent = GuardianConsent.builder()
                .user(user)
                .guardianName(request.getGuardianName())
                .guardianEmail(request.getGuardianEmail())
                .guardianPhone(request.getGuardianPhone())
                .consentAccepted(request.getConsentAccepted())
                .electronicSignature(request.getElectronicSignature())
                .consentDate(LocalDateTime.now())
                .build();
        guardianConsentRepository.save(consent);

        user.setStatus(UserStatus.UNDER_REVIEW);
        userRepository.save(user);

        eventPublisher.publishEvent(new GuardianConsentReceivedEvent(userId, request.getGuardianEmail()));

        log.info("Guardian consent submitted for user {}, status transitioned to UNDER_REVIEW", userId);

        AthleteProfile profile = athleteProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found"));
        return mapToResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public AthleteProfileResponse getProfile(UUID userId) {
        AthleteProfile profile = athleteProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found"));
        return mapToResponse(profile);
    }

    @Override
    public AthleteProfileResponse updateProfile(UUID userId, AthleteProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("Account is not in an active state");
        }

        AthleteProfile profile = athleteProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found"));

        // Update only provided fields
        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getStateOfResidence() != null) {
            profile.setStateOfResidence(request.getStateOfResidence());
        }
        if (request.getCountryOfResidence() != null) {
            profile.setCountryOfResidence(request.getCountryOfResidence());
        }
        if (request.getNationality() != null) {
            profile.setNationality(request.getNationality());
        }
        if (request.getHeightCm() != null) {
            profile.setHeightCm(request.getHeightCm());
        }
        if (request.getWeightKg() != null) {
            profile.setWeightKg(request.getWeightKg());
        }
        if (request.getSportsBackgrounds() != null) {
            profile.setSportsBackgrounds(request.getSportsBackgrounds());
        }
        if (request.getAthleteInterest() != null) {
            profile.setAthleteInterest(request.getAthleteInterest());
        }
        if (request.getDisabilityStatus() != null) {
            profile.setDisabilityStatus(request.getDisabilityStatus());
        }

        AthleteProfile savedProfile = athleteProfileRepository.save(profile);
        log.info("Profile updated for user {}", userId);
        return mapToResponse(savedProfile);
    }

    @Override
    public AthleteProfileResponse.VideoResponse addVideo(UUID userId, VideoAddRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new AccountNotActiveException("Account is not in an active state");
        }

        AthleteProfile profile = athleteProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found"));

        int videoCount = athleteVideoRepository.countByAthleteProfileId(profile.getId());
        if (videoCount >= 10) {
            throw new MaxLimitReachedException("Maximum video limit of 10 has been reached");
        }

        AthleteVideo video = AthleteVideo.builder()
                .athleteProfile(profile)
                .url(request.getUrl())
                .videoType(request.getVideoType())
                .build();
        AthleteVideo savedVideo = athleteVideoRepository.save(video);

        log.info("Video added for user {}", userId);
        return mapToVideoResponse(savedVideo);
    }

    @Override
    public void deleteVideo(UUID userId, UUID videoId) {
        AthleteVideo video = athleteVideoRepository.findByIdAndAthleteProfile_User_Id(videoId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found"));

        athleteVideoRepository.delete(video);
        log.info("Video {} deleted for user {}", videoId, userId);
    }

    @Override
    public AthleteProfileResponse uploadProfileImage(UUID userId, MultipartFile file) {
        validateImageFile(file);

        AthleteProfile profile = athleteProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found"));

        // Delete existing profile image if present
        if (profile.getProfileImageUrl() != null) {
            storageService.delete(extractRelativePath(profile.getProfileImageUrl()));
        }

        String storedPath = storageService.store(file, "athletes/" + userId + "/profile");
        profile.setProfileImageUrl(buildAbsoluteImageUrl(storedPath));
        AthleteProfile savedProfile = athleteProfileRepository.save(profile);

        log.info("Profile image uploaded for user {}", userId);
        return mapToResponse(savedProfile);
    }

    @Override
    public AthleteProfileResponse uploadIconImage(UUID userId, MultipartFile file) {
        validateImageFile(file);

        AthleteProfile profile = athleteProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Athlete profile not found"));

        // Delete existing icon image if present
        if (profile.getIconImageUrl() != null) {
            storageService.delete(extractRelativePath(profile.getIconImageUrl()));
        }

        String storedPath = storageService.store(file, "athletes/" + userId + "/icon");
        profile.setIconImageUrl(buildAbsoluteImageUrl(storedPath));
        AthleteProfile savedProfile = athleteProfileRepository.save(profile);

        log.info("Icon/passport image uploaded for user {}", userId);
        return mapToResponse(savedProfile);
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("File size must not exceed 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Only JPG, JPEG, and PNG image files are allowed");
        }
    }

    private AthleteProfileResponse mapToResponse(AthleteProfile profile) {
        List<AthleteProfileResponse.VideoResponse> videoResponses = profile.getVideos().stream()
                .map(this::mapToVideoResponse)
                .collect(Collectors.toList());

        return AthleteProfileResponse.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .age(profile.getAge())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender().name())
                .stateOfResidence(profile.getStateOfResidence())
                .countryOfResidence(profile.getCountryOfResidence())
                .nationality(profile.getNationality())
                .heightCm(profile.getHeightCm())
                .weightKg(profile.getWeightKg())
                .athleteInterest(profile.getAthleteInterest().name())
                .disabilityStatus(profile.isDisabilityStatus())
                .sportsBackgrounds(profile.getSportsBackgrounds().stream()
                        .map(Enum::name)
                        .collect(Collectors.toSet()))
                .profileImageUrl(profile.getProfileImageUrl())
                .iconImageUrl(profile.getIconImageUrl())
                .videos(videoResponses)
                .build();
    }

    private AthleteProfileResponse.VideoResponse mapToVideoResponse(AthleteVideo video) {
        return AthleteProfileResponse.VideoResponse.builder()
                .id(video.getId())
                .url(video.getUrl())
                .videoType(video.getVideoType().name())
                .createdDate(video.getCreatedDate())
                .build();
    }

    private String buildAbsoluteImageUrl(String relativePath) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        return base + "/storage/" + path;
    }

    private String extractRelativePath(String absoluteUrl) {
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String storagePrefix = base + "storage/";
        if (absoluteUrl.startsWith(storagePrefix)) {
            return absoluteUrl.substring(storagePrefix.length());
        }
        if (absoluteUrl.startsWith(base)) {
            return absoluteUrl.substring(base.length());
        }
        return absoluteUrl; // fallback: already a relative path
    }
}
