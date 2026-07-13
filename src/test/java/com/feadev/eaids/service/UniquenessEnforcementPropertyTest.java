package com.feadev.eaids.service;

import com.feadev.eaids.registrations.dto.request.AthleteRegistrationRequest;
import com.feadev.eaids.registrations.exception.DuplicateResourceException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for uniqueness enforcement.
 *
 * Property 1: Uniqueness Enforcement
 * Validates: Requirements 1.2, 1.3
 */
class UniquenessEnforcementPropertyTest {

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
    void duplicateEmailAlwaysThrowsDuplicateResourceException(
            @ForAll("validEmails") String email,
            @ForAll("validPhoneNumbers") String phoneNumber
    ) {
        // Setup: email already exists
        reset(userRepository);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        AthleteRegistrationRequest request = AthleteRegistrationRequest.builder()
                .email(email)
                .phoneNumber(phoneNumber)
                .password("SecureP@ss1")
                .confirmPassword("SecureP@ss1")
                .build();

        try {
            authService.registerAthlete(request);
            throw new AssertionError("Expected DuplicateResourceException for duplicate email: " + email);
        } catch (DuplicateResourceException e) {
            assert e.getMessage().contains("Email") : "Expected message about email but got: " + e.getMessage();
        }
    }

    @Property
    void duplicatePhoneNumberAlwaysThrowsDuplicateResourceException(
            @ForAll("validEmails") String email,
            @ForAll("validPhoneNumbers") String phoneNumber
    ) {
        // Setup: email is unique but phone already exists
        reset(userRepository);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.existsByPhoneNumber(phoneNumber)).thenReturn(true);

        AthleteRegistrationRequest request = AthleteRegistrationRequest.builder()
                .email(email)
                .phoneNumber(phoneNumber)
                .password("SecureP@ss1")
                .confirmPassword("SecureP@ss1")
                .build();

        try {
            authService.registerAthlete(request);
            throw new AssertionError("Expected DuplicateResourceException for duplicate phone: " + phoneNumber);
        } catch (DuplicateResourceException e) {
            assert e.getMessage().contains("Phone") : "Expected message about phone but got: " + e.getMessage();
        }
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
