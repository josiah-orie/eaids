package com.feadev.eaids.service;

import com.feadev.eaids.registrations.dto.request.VideoAddRequest;
import com.feadev.eaids.registrations.exception.MaxLimitReachedException;
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
import net.jqwik.api.constraints.IntRange;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for video count invariant.
 *
 * Property 12: Video Count Invariant
 * Validates: Requirements 4.6, 11.3, 11.4
 */
class VideoCountInvariantPropertyTest {

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
    void videoAdditionSucceedsWhenCountBelowLimit(
            @ForAll @IntRange(min = 0, max = 9) int currentVideoCount,
            @ForAll("videoTypes") VideoType videoType
    ) {
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        // Reset and setup mocks
        reset(userRepository, athleteProfileRepository, athleteVideoRepository);

        User user = User.builder()
                .id(userId)
                .email("athlete@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        AthleteProfile profile = AthleteProfile.builder()
                .id(profileId)
                .user(user)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(athleteProfileRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));
        when(athleteVideoRepository.countByAthleteProfileId(profileId)).thenReturn(currentVideoCount);
        when(athleteVideoRepository.save(any(AthleteVideo.class))).thenAnswer(invocation -> {
            AthleteVideo v = invocation.getArgument(0);
            v.setId(UUID.randomUUID());
            return v;
        });

        String url = getUrlForVideoType(videoType);
        VideoAddRequest request = VideoAddRequest.builder()
                .url(url)
                .videoType(videoType)
                .build();

        // Should succeed without exception
        var response = athleteService.addVideo(userId, request);
        assert response != null : "Expected a non-null response for valid video addition";
    }

    @Property
    void videoAdditionFailsWhenCountAtLimit(
            @ForAll("videoTypes") VideoType videoType
    ) {
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        // Reset and setup mocks
        reset(userRepository, athleteProfileRepository, athleteVideoRepository);

        User user = User.builder()
                .id(userId)
                .email("athlete@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        AthleteProfile profile = AthleteProfile.builder()
                .id(profileId)
                .user(user)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(athleteProfileRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));
        when(athleteVideoRepository.countByAthleteProfileId(profileId)).thenReturn(10);

        String url = getUrlForVideoType(videoType);
        VideoAddRequest request = VideoAddRequest.builder()
                .url(url)
                .videoType(videoType)
                .build();

        try {
            athleteService.addVideo(userId, request);
            throw new AssertionError("Expected MaxLimitReachedException when video count is at 10");
        } catch (MaxLimitReachedException e) {
            assert e.getMessage().contains("10")
                    : "Expected message to mention limit of 10 but got: " + e.getMessage();
        }
    }

    private String getUrlForVideoType(VideoType videoType) {
        return switch (videoType) {
            case YOUTUBE -> "https://www.youtube.com/watch?v=abc123";
            case HUDL -> "https://www.hudl.com/video/123";
            case GOOGLE_DRIVE -> "https://drive.google.com/file/d/abc/view";
        };
    }

    @Provide
    Arbitrary<VideoType> videoTypes() {
        return Arbitraries.of(VideoType.values());
    }
}
