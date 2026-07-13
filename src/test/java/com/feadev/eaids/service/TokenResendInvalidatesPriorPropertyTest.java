package com.feadev.eaids.service;

import com.feadev.eaids.registrations.dto.request.ResendVerificationRequest;
import com.feadev.eaids.registrations.dto.request.VerifyEmailRequest;
import com.feadev.eaids.registrations.dto.response.MessageResponse;
import com.feadev.eaids.registrations.exception.InvalidTokenException;
import com.feadev.eaids.registrations.model.EmailVerificationToken;
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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for token resend invalidates prior tokens.
 *
 * Property 8: Token Resend Invalidates Prior Tokens
 * Validates: Requirements 3.4
 */
class TokenResendInvalidatesPriorPropertyTest {

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
    void resendVerificationInvalidatesPriorToken(
            @ForAll("validEmails") String email
    ) {
        reset(userRepository, emailVerificationTokenRepository, emailService);

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email(email)
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .phoneVerified(false)
                .enabled(false)
                .build();

        // Existing unused token that should be invalidated
        EmailVerificationToken existingToken = EmailVerificationToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token(UUID.randomUUID().toString())
                .used(false)
                .expiresAt(LocalDateTime.now().plusHours(20))
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(emailVerificationTokenRepository.findByUser_IdAndUsedFalse(userId))
                .thenReturn(Optional.of(existingToken));
        when(emailVerificationTokenRepository.save(any(EmailVerificationToken.class)))
                .thenAnswer(i -> i.getArgument(0));

        ResendVerificationRequest request = new ResendVerificationRequest();
        request.setEmail(email);

        MessageResponse response = authService.resendVerification(request);

        // Verify the old token was marked as used
        assert existingToken.isUsed()
                : "Existing token should be marked as used after resend";

        // Verify a new token was saved (save called at least twice: once for old, once for new)
        verify(emailVerificationTokenRepository, atLeast(2)).save(any(EmailVerificationToken.class));
    }

    @Property
    void oldTokenFailsVerificationAfterResend(
            @ForAll("validEmails") String email
    ) {
        reset(userRepository, emailVerificationTokenRepository, emailService, auditService, eventPublisher);

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email(email)
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .phoneVerified(false)
                .enabled(false)
                .build();

        String oldTokenValue = UUID.randomUUID().toString();

        // The old token has been marked as used (simulating post-resend state)
        EmailVerificationToken oldToken = EmailVerificationToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token(oldTokenValue)
                .used(true) // Invalidated by resend
                .expiresAt(LocalDateTime.now().plusHours(20))
                .build();

        when(emailVerificationTokenRepository.findByToken(oldTokenValue))
                .thenReturn(Optional.of(oldToken));

        VerifyEmailRequest verifyRequest = new VerifyEmailRequest();
        verifyRequest.setToken(oldTokenValue);

        try {
            authService.verifyEmail(verifyRequest);
            throw new AssertionError("Expected InvalidTokenException for used (invalidated) token");
        } catch (InvalidTokenException e) {
            assert e.getMessage().contains("already been used")
                    : "Expected 'already been used' message but got: " + e.getMessage();
        }
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
        Arbitrary<String> tld = Arbitraries.of("com", "org", "net");

        return Combinators.combine(localPart, domain, tld)
                .as((local, dom, t) -> local + "@" + dom + "." + t);
    }
}
