package com.feadev.eaids.service;

import com.feadev.eaids.registrations.dto.request.LoginRequest;
import com.feadev.eaids.registrations.dto.response.AuthResponse;
import com.feadev.eaids.registrations.exception.AccountNotActiveException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for login status guard.
 *
 * Property 16: Login Status Guard
 * Validates: Requirements 8.1, 8.3
 */
class LoginStatusGuardPropertyTest {

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

    @Property(tries = 30)
    void nonActiveUsersAlwaysRejectedOnLogin(
            @ForAll("nonActiveStatuses") UserStatus status,
            @ForAll("validEmails") String email
    ) {
        // Setup
        reset(userRepository, auditService, tokenService);
        String rawPassword = "SecureP@ss1";

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .phoneNumber("+2348012345678")
                .password(passwordEncoder.encode(rawPassword))
                .role(UserRole.ROLE_ATHLETE)
                .status(status)
                .emailVerified(false)
                .phoneVerified(false)
                .enabled(false)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest();
        request.setIdentifier(email);
        request.setPassword(rawPassword);

        try {
            authService.login(request);
            throw new AssertionError("Expected AccountNotActiveException for status: " + status);
        } catch (AccountNotActiveException e) {
            assert e.getMessage().contains("not active")
                    : "Expected 'not active' message but got: " + e.getMessage();
        }
    }

    @Property(tries = 30)
    void activeUsersCanLoginSuccessfully(
            @ForAll("validEmails") String email,
            @ForAll("userRoles") UserRole role
    ) {
        // Setup
        reset(userRepository, auditService, tokenService);
        String rawPassword = "SecureP@ss1";

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .phoneNumber("+2348012345678")
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(tokenService.generateAccessToken(any(User.class))).thenReturn("mock-access-token");
        when(tokenService.generateRefreshToken(any(User.class))).thenReturn("mock-refresh-token");

        LoginRequest request = new LoginRequest();
        request.setIdentifier(email);
        request.setPassword(rawPassword);

        AuthResponse response = authService.login(request);

        assert response.getAccessToken() != null : "Expected access token in response";
        assert response.getRefreshToken() != null : "Expected refresh token in response";
        assert response.getUser().getStatus().equals(UserStatus.ACTIVE.name())
                : "Expected ACTIVE status in response";
    }

    @Provide
    Arbitrary<UserStatus> nonActiveStatuses() {
        return Arbitraries.of(
                UserStatus.PENDING_VERIFICATION,
                UserStatus.PENDING_CONSENT,
                UserStatus.UNDER_REVIEW,
                UserStatus.REJECTED,
                UserStatus.SUSPENDED,
                UserStatus.ARCHIVED
        );
    }

    @Provide
    Arbitrary<UserRole> userRoles() {
        return Arbitraries.of(UserRole.ROLE_ATHLETE, UserRole.ROLE_COACH, UserRole.ROLE_ADMIN);
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
}
