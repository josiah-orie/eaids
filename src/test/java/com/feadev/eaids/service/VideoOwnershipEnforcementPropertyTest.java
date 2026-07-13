package com.feadev.eaids.service;

import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.AthleteProfile;
import com.feadev.eaids.registrations.model.AthleteVideo;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.model.enums.UserStatus;
import com.feadev.eaids.registrations.model.enums.VideoType;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.registrations.repository.AthleteVideoRepository;
import com.feadev.eaids.registrations.repository.GuardianConsentRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.registrations.service.StorageService;
import com.feadev.eaids.registrations.service.impl.AthleteServiceImpl;
import net.jqwik.api.*;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Property-based tests for video ownership enforcement.
 *
 * Property 24: Video Ownership Enforcement
 * Validates: Requirements 11.5, 11.6
 *
 * Verifies deletion succeeds for own videos and is rejected for others' videos.
 */
class VideoOwnershipEnforcementPropertyTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AthleteProfileRepository athleteProfileRepository = mock(AthleteProfileRepository.class);
    private final AthleteVideoRepository athleteVideoRepository = mock(AthleteVideoRepository.class);
    private final GuardianConsentRepository guardianConsentRepository = mock(GuardianConsentRepository.class);
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);
    private final AuditService auditService = mock(AuditService.class);
    private final StorageService storageService = mock(StorageService.class);

    private final AthleteServiceImpl athleteService = new AthleteServiceImpl(
            userRepository,
            athleteProfileRepository,
            athleteVideoRepository,
            guardianConsentRepository,
            eventPublisher,
            auditService,
            storageService
    );

    @Property
    void ownerCanDeleteOwnVideo(
            @ForAll("videoTypes") VideoType videoType
    ) {
        UUID userId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        reset(athleteVideoRepository);

        AthleteProfile profile = AthleteProfile.builder()
                .id(profileId)
                .user(User.builder()
                        .id(userId)
                        .email("athlete@example.com")
                        .phoneNumber("+2348012345678")
                        .password("encoded")
                        .role(UserRole.ROLE_ATHLETE)
                        .status(UserStatus.ACTIVE)
                        .build())
                .build();

        AthleteVideo video = AthleteVideo.builder()
                .id(videoId)
                .athleteProfile(profile)
                .url("https://www.youtube.com/watch?v=abc123")
                .videoType(videoType)
                .build();

        // findByIdAndAthleteProfile_User_Id returns the video (owner matches)
        when(athleteVideoRepository.findByIdAndAthleteProfile_User_Id(videoId, userId))
                .thenReturn(Optional.of(video));

        // Should succeed without exception
        athleteService.deleteVideo(userId, videoId);

        verify(athleteVideoRepository).delete(video);
    }

    @Property
    void nonOwnerCannotDeleteOthersVideo(
            @ForAll("videoTypes") VideoType videoType
    ) {
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID videoId = UUID.randomUUID();

        reset(athleteVideoRepository);

        // findByIdAndAthleteProfile_User_Id returns empty (requester is not the owner)
        when(athleteVideoRepository.findByIdAndAthleteProfile_User_Id(videoId, requesterId))
                .thenReturn(Optional.empty());

        try {
            athleteService.deleteVideo(requesterId, videoId);
            throw new AssertionError(
                    "Expected ResourceNotFoundException when non-owner attempts deletion");
        } catch (ResourceNotFoundException e) {
            // The service throws ResourceNotFoundException when video is not found for the user
            assert e.getMessage().toLowerCase().contains("video")
                    || e.getMessage().toLowerCase().contains("not found")
                    : "Expected message about video not found but got: " + e.getMessage();
        }
    }

    @Property
    void deletionOfNonExistentVideoFails(
            @ForAll("randomUUIDs") UUID userId,
            @ForAll("randomUUIDs") UUID videoId
    ) {
        reset(athleteVideoRepository);

        when(athleteVideoRepository.findByIdAndAthleteProfile_User_Id(videoId, userId))
                .thenReturn(Optional.empty());

        try {
            athleteService.deleteVideo(userId, videoId);
            throw new AssertionError("Expected ResourceNotFoundException for non-existent video");
        } catch (ResourceNotFoundException e) {
            // Expected - video not found
            assert e.getMessage() != null : "Exception should have a message";
        }
    }

    @Provide
    Arbitrary<VideoType> videoTypes() {
        return Arbitraries.of(VideoType.values());
    }

    @Provide
    Arbitrary<UUID> randomUUIDs() {
        return Arbitraries.randomValue(random -> UUID.randomUUID());
    }
}
