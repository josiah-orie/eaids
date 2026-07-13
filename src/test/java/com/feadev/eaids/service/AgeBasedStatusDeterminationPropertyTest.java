package com.feadev.eaids.service;

import com.feadev.eaids.registrations.dto.request.AthleteProfileCompletionRequest;
import com.feadev.eaids.registrations.dto.request.VideoAddRequest;
import com.feadev.eaids.registrations.model.AthleteProfile;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for age-based status determination.
 *
 * Property 9: Age-Based Status Determination
 * Validates: Requirements 4.7, 4.8
 */
class AgeBasedStatusDeterminationPropertyTest {

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
    void dateOfBirthMakingAgeUnder18ResultsInPendingConsent(
            @ForAll("datesOfBirthUnder18") LocalDate dateOfBirth
    ) {
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        // Reset and setup mocks
        reset(userRepository, athleteProfileRepository, athleteVideoRepository);

        User user = User.builder()
                .id(userId)
                .email("minor@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(athleteProfileRepository.save(any(AthleteProfile.class))).thenAnswer(invocation -> {
            AthleteProfile p = invocation.getArgument(0);
            p.setId(profileId);
            p.setVideos(new ArrayList<>());
            return p;
        });
        when(athleteVideoRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        AthleteProfileCompletionRequest request = buildProfileRequest(dateOfBirth);

        athleteService.completeProfile(userId, request);

        assert user.getStatus() == UserStatus.PENDING_CONSENT
                : "Expected PENDING_CONSENT for age < 18 (dob=" + dateOfBirth + ") but got " + user.getStatus();
    }

    @Property
    void dateOfBirthMakingAge18OrOverResultsInUnderReview(
            @ForAll("datesOfBirth18OrOver") LocalDate dateOfBirth
    ) {
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        // Reset and setup mocks
        reset(userRepository, athleteProfileRepository, athleteVideoRepository);

        User user = User.builder()
                .id(userId)
                .email("adult@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(athleteProfileRepository.save(any(AthleteProfile.class))).thenAnswer(invocation -> {
            AthleteProfile p = invocation.getArgument(0);
            p.setId(profileId);
            p.setVideos(new ArrayList<>());
            return p;
        });
        when(athleteVideoRepository.saveAll(anyList())).thenReturn(Collections.emptyList());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        AthleteProfileCompletionRequest request = buildProfileRequest(dateOfBirth);

        athleteService.completeProfile(userId, request);

        assert user.getStatus() == UserStatus.UNDER_REVIEW
                : "Expected UNDER_REVIEW for age >= 18 (dob=" + dateOfBirth + ") but got " + user.getStatus();
    }

    private AthleteProfileCompletionRequest buildProfileRequest(LocalDate dateOfBirth) {
        VideoAddRequest video = VideoAddRequest.builder()
                .url("https://www.youtube.com/watch?v=abc123")
                .videoType(VideoType.YOUTUBE)
                .build();

        return AthleteProfileCompletionRequest.builder()
                .fullName("Test Athlete")
                .dateOfBirth(dateOfBirth)
                .gender(Gender.MALE)
                .stateOfResidence("Lagos")
                .countryOfResidence("Nigeria")
                .nationality("Nigerian")
                .heightCm(new BigDecimal("175.0"))
                .weightKg(new BigDecimal("70.0"))
                .sportsBackgrounds(Set.of(SportsBackground.ATHLETICS))
                .athleteInterest(AthleteInterest.TACKLE_FOOTBALL)
                .videos(List.of(video))
                .build();
    }

    @Provide
    Arbitrary<LocalDate> datesOfBirthUnder18() {
        // Generate dates that make the person under 18 right now
        // Born between 1 day ago and 17 years + 364 days ago
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.minusYears(17).minusDays(364);
        LocalDate maxDate = today.minusDays(1);

        return Arbitraries.longs()
                .between(minDate.toEpochDay(), maxDate.toEpochDay())
                .map(LocalDate::ofEpochDay)
                .filter(dob -> Period.between(dob, LocalDate.now()).getYears() < 18);
    }

    @Provide
    Arbitrary<LocalDate> datesOfBirth18OrOver() {
        // Generate dates that make the person 18 or older
        // Born between 18 years ago and 80 years ago
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.minusYears(80);
        LocalDate maxDate = today.minusYears(18);

        return Arbitraries.longs()
                .between(minDate.toEpochDay(), maxDate.toEpochDay())
                .map(LocalDate::ofEpochDay)
                .filter(dob -> Period.between(dob, LocalDate.now()).getYears() >= 18);
    }
}
