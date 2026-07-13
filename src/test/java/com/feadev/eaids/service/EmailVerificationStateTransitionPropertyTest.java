package com.feadev.eaids.service;

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
 * Property-based tests for email verification state transition.
 *
 * Property 7: Email Verification State Transition
 * Validates: Requirements 3.1, 3.7
 */
class EmailVerificationStateTransitionPropertyTest {

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
    void validTokenWithPendingVerificationUserSetsEmailVerifiedTrue(
            @ForAll("validTokenStrings") String tokenString
    ) {
        // Setup
        reset(userRepository, emailVerificationTokenRepository, auditService, eventPublisher);

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .phoneVerified(false)
                .enabled(false)
                .build();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token(tokenString)
                .used(false)
                .expiresAt(LocalDateTime.now().plusHours(23))
                .build();

        when(emailVerificationTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));
        when(emailVerificationTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setToken(tokenString);

        MessageResponse response = authService.verifyEmail(request);

        assert user.isEmailVerified() : "Expected emailVerified to be true after verification";
        assert response.getMessage().contains("verified") : "Expected success message";
    }

    @Property
    void tokenForNonPendingVerificationUserShouldBeRejected(
            @ForAll("validTokenStrings") String tokenString,
            @ForAll("nonPendingVerificationStatuses") UserStatus status
    ) {
        // Setup
        reset(userRepository, emailVerificationTokenRepository, auditService, eventPublisher);

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_ATHLETE)
                .status(status)
                .emailVerified(false)
                .phoneVerified(false)
                .enabled(false)
                .build();

        EmailVerificationToken token = EmailVerificationToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token(tokenString)
                .used(false)
                .expiresAt(LocalDateTime.now().plusHours(23))
                .build();

        when(emailVerificationTokenRepository.findByToken(tokenString)).thenReturn(Optional.of(token));

        VerifyEmailRequest request = new VerifyEmailRequest();
        request.setToken(tokenString);

        try {
            authService.verifyEmail(request);
            throw new AssertionError("Expected InvalidTokenException for status " + status);
        } catch (InvalidTokenException e) {
            assert e.getMessage().contains("not applicable")
                    : "Expected 'not applicable' message but got: " + e.getMessage();
        }
    }

    @Provide
    Arbitrary<String> validTokenStrings() {
        return Arbitraries.create(() -> UUID.randomUUID().toString());
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
