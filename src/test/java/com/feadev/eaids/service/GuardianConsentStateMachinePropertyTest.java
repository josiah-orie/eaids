package com.feadev.eaids.service;

import com.feadev.eaids.registrations.dto.request.GuardianConsentRequest;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
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
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for guardian consent state machine.
 *
 * Property 13: Guardian Consent State Machine
 * Validates: Requirements 5.1, 5.2, 5.6
 */
class GuardianConsentStateMachinePropertyTest {

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
    void pendingConsentStatusAllowsConsentSubmissionAndTransitionsToUnderReview(
            @ForAll("guardianNames") String guardianName,
            @ForAll("guardianEmails") String guardianEmail,
            @ForAll("guardianPhones") String guardianPhone,
            @ForAll("signatures") String signature
    ) {
        UUID userId = UUID.randomUUID();

        // Reset and setup mocks
        reset(userRepository, athleteProfileRepository, guardianConsentRepository, eventPublisher);

        User user = User.builder()
                .id(userId)
                .email("athlete@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.PENDING_CONSENT)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(false)
                .build();

        AthleteProfile profile = AthleteProfile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .fullName("Minor Athlete")
                .age(16)
                .dateOfBirth(LocalDate.now().minusYears(16))
                .gender(Gender.MALE)
                .stateOfResidence("Lagos")
                .countryOfResidence("Nigeria")
                .nationality("Nigerian")
                .heightCm(new BigDecimal("170.0"))
                .weightKg(new BigDecimal("60.0"))
                .athleteInterest(AthleteInterest.TACKLE_FOOTBALL)
                .sportsBackgrounds(Set.of(SportsBackground.ATHLETICS))
                .videos(new ArrayList<>())
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(guardianConsentRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(athleteProfileRepository.findByUser_Id(userId)).thenReturn(Optional.of(profile));

        GuardianConsentRequest request = GuardianConsentRequest.builder()
                .guardianName(guardianName)
                .guardianEmail(guardianEmail)
                .guardianPhone(guardianPhone)
                .consentAccepted(true)
                .electronicSignature(signature)
                .build();

        athleteService.submitGuardianConsent(userId, request);

        assert user.getStatus() == UserStatus.UNDER_REVIEW
                : "Expected UNDER_REVIEW after guardian consent but got " + user.getStatus();
    }

    @Property
    void nonPendingConsentStatusRejectsConsentSubmission(
            @ForAll("nonPendingConsentStatuses") UserStatus status
    ) {
        UUID userId = UUID.randomUUID();

        // Reset and setup mocks
        reset(userRepository);

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

        GuardianConsentRequest request = GuardianConsentRequest.builder()
                .guardianName("Guardian Name")
                .guardianEmail("guardian@example.com")
                .guardianPhone("+2348099999999")
                .consentAccepted(true)
                .electronicSignature("I consent")
                .build();

        try {
            athleteService.submitGuardianConsent(userId, request);
            throw new AssertionError("Expected InvalidStateTransitionException for status " + status);
        } catch (InvalidStateTransitionException e) {
            assert e.getMessage().contains("not applicable")
                    : "Expected 'not applicable' message but got: " + e.getMessage();
        }
    }

    @Provide
    Arbitrary<String> guardianNames() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withChars(' ')
                .ofMinLength(2)
                .ofMaxLength(50)
                .filter(s -> !s.isBlank());
    }

    @Provide
    Arbitrary<String> guardianEmails() {
        Arbitrary<String> localPart = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .ofMinLength(3)
                .ofMaxLength(10);
        Arbitrary<String> domain = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(3)
                .ofMaxLength(8);

        return Combinators.combine(localPart, domain)
                .as((local, dom) -> local + "@" + dom + ".com")
                .filter(email -> !email.equalsIgnoreCase("athlete@example.com"));
    }

    @Provide
    Arbitrary<String> guardianPhones() {
        return Arbitraries.strings()
                .withCharRange('0', '9')
                .ofMinLength(7)
                .ofMaxLength(14)
                .map(digits -> "+" + digits);
    }

    @Provide
    Arbitrary<String> signatures() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withChars(' ')
                .ofMinLength(5)
                .ofMaxLength(100)
                .filter(s -> !s.isBlank());
    }

    @Provide
    Arbitrary<UserStatus> nonPendingConsentStatuses() {
        return Arbitraries.of(
                UserStatus.PENDING_VERIFICATION,
                UserStatus.UNDER_REVIEW,
                UserStatus.ACTIVE,
                UserStatus.REJECTED,
                UserStatus.SUSPENDED,
                UserStatus.ARCHIVED
        );
    }
}
