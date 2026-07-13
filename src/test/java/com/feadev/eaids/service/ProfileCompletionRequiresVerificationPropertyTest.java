package com.feadev.eaids.service;

import com.feadev.eaids.registrations.dto.request.AthleteProfileCompletionRequest;
import com.feadev.eaids.registrations.dto.request.VideoAddRequest;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.*;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.registrations.repository.AthleteVideoRepository;
import com.feadev.eaids.registrations.repository.GuardianConsentRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.registrations.service.StorageService;
import com.feadev.eaids.registrations.service.impl.AthleteServiceImpl;
import net.jqwik.api.*;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Property-based tests for profile completion requiring email verification.
 *
 * Property 10: Profile Completion Requires Email Verification
 * Validates: Requirements 4.2
 *
 * For any user without emailVerified=true or not in PENDING_VERIFICATION status,
 * profile completion is rejected.
 */
class ProfileCompletionRequiresVerificationPropertyTest {

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
    void userWithoutEmailVerificationCannotCompleteProfile(
            @ForAll("nonPendingVerificationStatuses") UserStatus status
    ) {
        UUID userId = UUID.randomUUID();

        reset(userRepository);

        // User has emailVerified=false and status != PENDING_VERIFICATION
        User user = User.builder()
                .id(userId)
                .email("athlete@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_ATHLETE)
                .status(status)
                .emailVerified(false)
                .phoneVerified(false)
                .enabled(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        AthleteProfileCompletionRequest request = buildValidProfileRequest();

        try {
            athleteService.completeProfile(userId, request);
            throw new AssertionError(
                    "Expected InvalidStateTransitionException for unverified user with status " + status);
        } catch (InvalidStateTransitionException e) {
            assert e.getMessage().toLowerCase().contains("email")
                    || e.getMessage().toLowerCase().contains("verified")
                    : "Expected message about email verification but got: " + e.getMessage();
        }
    }

    @Property
    void userWithEmailVerifiedButWrongStatusCannotCompleteProfile(
            @ForAll("nonPendingVerificationStatuses") UserStatus status
    ) {
        UUID userId = UUID.randomUUID();

        reset(userRepository);

        // User has emailVerified=true but status != PENDING_VERIFICATION
        User user = User.builder()
                .id(userId)
                .email("athlete@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_ATHLETE)
                .status(status)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        AthleteProfileCompletionRequest request = buildValidProfileRequest();

        try {
            athleteService.completeProfile(userId, request);
            throw new AssertionError(
                    "Expected InvalidStateTransitionException for verified user with status " + status);
        } catch (InvalidStateTransitionException e) {
            assert e.getMessage().toLowerCase().contains("email")
                    || e.getMessage().toLowerCase().contains("verified")
                    || e.getMessage().toLowerCase().contains("profile")
                    : "Expected message about verification requirement but got: " + e.getMessage();
        }
    }

    @Property
    void unverifiedEmailWithPendingVerificationStatusCannotCompleteProfile() {
        UUID userId = UUID.randomUUID();

        reset(userRepository);

        // User has status PENDING_VERIFICATION but emailVerified=false
        User user = User.builder()
                .id(userId)
                .email("athlete@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .phoneVerified(false)
                .enabled(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        AthleteProfileCompletionRequest request = buildValidProfileRequest();

        try {
            athleteService.completeProfile(userId, request);
            throw new AssertionError(
                    "Expected InvalidStateTransitionException for emailVerified=false with PENDING_VERIFICATION");
        } catch (InvalidStateTransitionException e) {
            assert e.getMessage().toLowerCase().contains("email")
                    || e.getMessage().toLowerCase().contains("verified")
                    : "Expected message about email verification but got: " + e.getMessage();
        }
    }

    private AthleteProfileCompletionRequest buildValidProfileRequest() {
        VideoAddRequest video = VideoAddRequest.builder()
                .url("https://www.youtube.com/watch?v=abc123")
                .videoType(VideoType.YOUTUBE)
                .build();

        return AthleteProfileCompletionRequest.builder()
                .fullName("Test Athlete")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .gender(Gender.MALE)
                .stateOfResidence("Lagos")
                .countryOfResidence("Nigeria")
                .nationality("Nigerian")
                .heightCm(new java.math.BigDecimal("180.0"))
                .weightKg(new java.math.BigDecimal("80.0"))
                .sportsBackgrounds(Set.of(SportsBackground.ATHLETICS))
                .athleteInterest(AthleteInterest.TACKLE_FOOTBALL)
                .videos(List.of(video))
                .build();
    }

    @Provide
    Arbitrary<UserStatus> nonPendingVerificationStatuses() {
        return Arbitraries.of(
                UserStatus.PENDING_CONSENT,
                UserStatus.UNDER_REVIEW,
                UserStatus.ACTIVE,
                UserStatus.REJECTED,
                UserStatus.SUSPENDED,
                UserStatus.ARCHIVED
        );
    }
}
