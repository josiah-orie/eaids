package com.feadev.eaids.service;

import com.feadev.eaids.registrations.dto.request.LoginRequest;
import com.feadev.eaids.registrations.exception.InvalidTokenException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.model.enums.UserStatus;
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

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * Property-based tests for generic authentication error.
 *
 * Property 19: Generic Authentication Error
 * Validates: Requirements 8.4
 */
class GenericAuthenticationErrorPropertyTest {

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

    @Property(tries = 20)
    void wrongEmailAndWrongPasswordProduceSameErrorMessage(
            @ForAll("validEmails") String existingEmail,
            @ForAll("validEmails") String nonExistingEmail,
            @ForAll("validPasswords") String correctPassword,
            @ForAll("validPasswords") String wrongPassword
    ) {
        Assume.that(!existingEmail.equals(nonExistingEmail));
        Assume.that(!correctPassword.equals(wrongPassword));

        reset(userRepository, auditService);

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(existingEmail)
                .phoneNumber("+2348012345678")
                .password(passwordEncoder.encode(correctPassword))
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        // Case 1: Non-existing email (wrong email)
        when(userRepository.findByEmail(nonExistingEmail)).thenReturn(Optional.empty());
        when(userRepository.findByPhoneNumber(nonExistingEmail)).thenReturn(Optional.empty());

        LoginRequest wrongEmailRequest = new LoginRequest();
        wrongEmailRequest.setIdentifier(nonExistingEmail);
        wrongEmailRequest.setPassword(correctPassword);

        String wrongEmailMessage;
        try {
            authService.login(wrongEmailRequest);
            throw new AssertionError("Expected exception for wrong email");
        } catch (InvalidTokenException e) {
            wrongEmailMessage = e.getMessage();
        }

        // Case 2: Correct email but wrong password
        reset(userRepository, auditService);
        when(userRepository.findByEmail(existingEmail)).thenReturn(Optional.of(user));

        LoginRequest wrongPasswordRequest = new LoginRequest();
        wrongPasswordRequest.setIdentifier(existingEmail);
        wrongPasswordRequest.setPassword(wrongPassword);

        String wrongPasswordMessage;
        try {
            authService.login(wrongPasswordRequest);
            throw new AssertionError("Expected exception for wrong password");
        } catch (InvalidTokenException e) {
            wrongPasswordMessage = e.getMessage();
        }

        assert wrongEmailMessage.equals(wrongPasswordMessage)
                : "Error messages must be identical to prevent information leakage. " +
                  "Wrong email message: '" + wrongEmailMessage + "', " +
                  "Wrong password message: '" + wrongPasswordMessage + "'";
    }

    @Provide
    Arbitrary<String> validEmails() {
        Arbitrary<String> localPart = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('0', '9')
                .ofMinLength(3)
                .ofMaxLength(10);
        Arbitrary<String> domain = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(3)
                .ofMaxLength(8);
        Arbitrary<String> tld = Arbitraries.of("com", "org", "net", "io");

        return Combinators.combine(localPart, domain, tld)
                .as((local, dom, t) -> local + "@" + dom + "." + t);
    }

    @Provide
    Arbitrary<String> validPasswords() {
        Arbitrary<String> upperPart = Arbitraries.strings()
                .withCharRange('A', 'Z')
                .ofMinLength(1)
                .ofMaxLength(2);
        Arbitrary<String> lowerPart = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(3)
                .ofMaxLength(8);
        Arbitrary<String> digitPart = Arbitraries.strings()
                .withCharRange('0', '9')
                .ofMinLength(1)
                .ofMaxLength(2);
        Arbitrary<String> specialPart = Arbitraries.of("@", "#", "$", "%", "!");

        return Combinators.combine(upperPart, lowerPart, digitPart, specialPart)
                .as((upper, lower, digit, special) -> upper + lower + digit + special);
    }
}
