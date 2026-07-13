package com.feadev.eaids.service;

import com.feadev.eaids.registrations.exception.InvalidTokenException;
import com.feadev.eaids.registrations.exception.OtpExhaustedException;
import com.feadev.eaids.registrations.model.OtpToken;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.model.enums.UserStatus;
import com.feadev.eaids.registrations.repository.OtpTokenRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.impl.OtpServiceImpl;
import net.jqwik.api.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for OTP brute-force protection.
 *
 * Property 20: OTP Brute-Force Protection
 * Validates: Requirements 9.5
 */
class OtpBruteForceProtectionPropertyTest {

    private final OtpTokenRepository otpTokenRepository = mock(OtpTokenRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);

    private final OtpServiceImpl otpService = new OtpServiceImpl(otpTokenRepository, userRepository);

    @Property
    void afterFiveFailedAttemptsOtpIsInvalidated(
            @ForAll("validEmails") String email,
            @ForAll("sixDigitOtps") String correctOtp,
            @ForAll("wrongOtps") String wrongOtp
    ) {
        Assume.that(!correctOtp.equals(wrongOtp));

        reset(otpTokenRepository, userRepository);

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email(email)
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        OtpToken otpToken = OtpToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .otpCode(correctOtp)
                .failedAttempts(0)
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(otpTokenRepository.findByUser_IdAndUsedFalse(userId)).thenReturn(Optional.of(otpToken));
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(i -> i.getArgument(0));

        // Submit 4 wrong attempts — should throw InvalidTokenException
        for (int i = 0; i < 4; i++) {
            try {
                otpService.verifyOtp(email, wrongOtp);
                throw new AssertionError("Expected InvalidTokenException on attempt " + (i + 1));
            } catch (InvalidTokenException e) {
                // Expected
            }
        }

        // 5th wrong attempt — should throw OtpExhaustedException and invalidate OTP
        try {
            otpService.verifyOtp(email, wrongOtp);
            throw new AssertionError("Expected OtpExhaustedException on 5th failed attempt");
        } catch (OtpExhaustedException e) {
            assert e.getMessage().contains("invalidated")
                    : "Expected 'invalidated' message but got: " + e.getMessage();
            assert otpToken.isUsed() : "OTP should be marked as used after 5 failed attempts";
            assert otpToken.getFailedAttempts() >= 5
                    : "Expected failedAttempts >= 5 but got: " + otpToken.getFailedAttempts();
        }
    }

    @Property
    void correctOtpBeforeFiveFailedAttemptsSucceeds(
            @ForAll("validEmails") String email,
            @ForAll("sixDigitOtps") String correctOtp
    ) {
        reset(otpTokenRepository, userRepository);

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email(email)
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(UserRole.ROLE_ATHLETE)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        OtpToken otpToken = OtpToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .otpCode(correctOtp)
                .failedAttempts(3) // Some failed attempts, but less than 5
                .used(false)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(otpTokenRepository.findByUser_IdAndUsedFalse(userId)).thenReturn(Optional.of(otpToken));
        when(otpTokenRepository.save(any(OtpToken.class))).thenAnswer(i -> i.getArgument(0));

        User result = otpService.verifyOtp(email, correctOtp);

        assert result.getId().equals(userId) : "Expected the correct user returned";
        assert otpToken.isUsed() : "OTP should be marked as used after successful verification";
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

    @Provide
    Arbitrary<String> sixDigitOtps() {
        return Arbitraries.integers().between(100000, 999999).map(String::valueOf);
    }

    @Provide
    Arbitrary<String> wrongOtps() {
        // Generate OTPs that are always different from the "correct" ones
        return Arbitraries.integers().between(100000, 499999).map(String::valueOf);
    }
}
