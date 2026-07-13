package com.feadev.eaids.service;

import com.feadev.eaids.registrations.dto.request.AthleteRegistrationRequest;
import com.feadev.eaids.registrations.dto.request.CoachRegistrationRequest;
import com.feadev.eaids.registrations.dto.response.RegistrationResponse;
import com.feadev.eaids.registrations.model.CoachProfile;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.*;
import com.feadev.eaids.registrations.repository.CoachProfileRepository;
import com.feadev.eaids.registrations.repository.EmailVerificationTokenRepository;
import com.feadev.eaids.registrations.repository.PasswordResetTokenRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.registrations.service.EmailService;
import com.feadev.eaids.registrations.service.OtpService;
import com.feadev.eaids.registrations.service.TokenService;
import com.feadev.eaids.registrations.service.impl.AuthServiceImpl;
import net.jqwik.api.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for registration assigns correct role and status.
 *
 * Property 6: Registration Assigns Correct Role and Initial Status
 * Validates: Requirements 2.1, 6.1
 */
class RegistrationRoleStatusPropertyTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CoachProfileRepository coachProfileRepository = mock(CoachProfileRepository.class);
    private final EmailVerificationTokenRepository emailVerificationTokenRepository = mock(EmailVerificationTokenRepository.class);
    private final PasswordResetTokenRepository passwordResetTokenRepository = mock(PasswordResetTokenRepository.class);
    private final TokenService tokenService = mock(TokenService.class);
    private final OtpService otpService = mock(OtpService.class);
    private final EmailService emailService = mock(EmailService.class);
    private final AuditService auditService = mock(AuditService.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

    private final AuthServiceImpl authService = new AuthServiceImpl(
            userRepository,
            coachProfileRepository,
            emailVerificationTokenRepository,
            passwordResetTokenRepository,
            tokenService,
            otpService,
            emailService,
            auditService,
            passwordEncoder,
            eventPublisher
    );

    @Property
    void athleteRegistrationAlwaysAssignsRoleAthleteAndPendingVerification(
            @ForAll("validEmails") String email,
            @ForAll("validPhoneNumbers") String phoneNumber
    ) {
        // Setup mocks
        reset(userRepository, emailVerificationTokenRepository, emailService, eventPublisher, auditService);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(emailVerificationTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        AthleteRegistrationRequest request = AthleteRegistrationRequest.builder()
                .email(email)
                .phoneNumber(phoneNumber)
                .password("SecureP@ss1")
                .confirmPassword("SecureP@ss1")
                .build();

        RegistrationResponse response = authService.registerAthlete(request);

        assert response.getRole().equals("ROLE_ATHLETE")
                : "Expected ROLE_ATHLETE but got " + response.getRole();
        assert response.getStatus().equals("PENDING_VERIFICATION")
                : "Expected PENDING_VERIFICATION but got " + response.getStatus();
    }

    @Property
    void coachRegistrationAlwaysAssignsRoleCoachAndPendingVerification(
            @ForAll("validEmails") String email,
            @ForAll("validPhoneNumbers") String phoneNumber
    ) {
        // Setup mocks
        reset(userRepository, coachProfileRepository, emailVerificationTokenRepository, emailService, eventPublisher, auditService);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(coachProfileRepository.save(any(CoachProfile.class))).thenAnswer(i -> i.getArgument(0));
        when(emailVerificationTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CoachRegistrationRequest request = CoachRegistrationRequest.builder()
                .email(email)
                .phoneNumber(phoneNumber)
                .password("SecureP@ss1")
                .confirmPassword("SecureP@ss1")
                .fullName("Coach Test")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .nationality("Nigerian")
                .stateOfResidence("Lagos")
                .areaOfInterest(AreaOfInterest.COACH)
                .availability(Availability.FULL_TIME)
                .americanFootballExperience(AmericanFootballExperience.FLAG_FOOTBALL)
                .involvementLevel(InvolvementLevel.NATIONAL)
                .coachingInterests(Set.of(CoachingInterest.YOUTH_DEVELOPMENT))
                .build();

        RegistrationResponse response = authService.registerCoach(request);

        assert response.getRole().equals("ROLE_COACH")
                : "Expected ROLE_COACH but got " + response.getRole();
        assert response.getStatus().equals("PENDING_VERIFICATION")
                : "Expected PENDING_VERIFICATION but got " + response.getStatus();
    }

    @Provide
    Arbitrary<String> validEmails() {
        Arbitrary<String> localPart = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .ofMinLength(3)
                .ofMaxLength(15);
        Arbitrary<String> domain = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(3)
                .ofMaxLength(10);
        Arbitrary<String> tld = Arbitraries.of("com", "org", "net", "io", "ng");

        return Combinators.combine(localPart, domain, tld)
                .as((local, dom, t) -> local + "@" + dom + "." + t);
    }

    @Provide
    Arbitrary<String> validPhoneNumbers() {
        return Arbitraries.strings()
                .withCharRange('0', '9')
                .ofMinLength(7)
                .ofMaxLength(14)
                .map(digits -> "+" + digits);
    }
}
