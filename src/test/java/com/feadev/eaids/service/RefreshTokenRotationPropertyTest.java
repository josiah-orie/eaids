package com.feadev.eaids.service;

import com.feadev.eaids.config.JwtConfig;
import com.feadev.eaids.registrations.exception.InvalidTokenException;
import com.feadev.eaids.registrations.model.RefreshToken;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.model.enums.UserStatus;
import com.feadev.eaids.registrations.repository.RefreshTokenRepository;
import com.feadev.eaids.security.JwtTokenProvider;
import com.feadev.eaids.registrations.service.impl.TokenServiceImpl;
import net.jqwik.api.*;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for refresh token rotation.
 *
 * Property 18: Refresh Token Rotation
 * Validates: Requirements 8.6, 8.7
 */
class RefreshTokenRotationPropertyTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("test-secret-key-for-jwt-property-tests-must-be-at-least-256-bits".getBytes());

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, 3600);
    private final RefreshTokenRepository refreshTokenRepository = mock(RefreshTokenRepository.class);
    private final JwtConfig jwtConfig = createJwtConfig();

    private final TokenServiceImpl tokenService = new TokenServiceImpl(
            jwtTokenProvider,
            refreshTokenRepository,
            jwtConfig
    );

    private JwtConfig createJwtConfig() {
        JwtConfig config = new JwtConfig();
        config.setSecret(SECRET);
        config.setAccessTokenExpiration(3600);
        config.setRefreshTokenExpiration(604800); // 7 days
        return config;
    }

    @Property
    void validatingRefreshTokenSucceedsForNonRevokedNonExpiredToken(
            @ForAll("userRoles") UserRole role
    ) {
        reset(refreshTokenRepository);

        String tokenValue = UUID.randomUUID().toString();
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(role)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token(tokenValue)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(refreshToken));

        RefreshToken result = tokenService.validateRefreshToken(tokenValue);

        assert result != null : "Expected non-null RefreshToken";
        assert result.getUser().equals(user) : "Expected user to match";
        assert !result.isRevoked() : "Expected token not to be revoked";
    }

    @Property
    void revokingRefreshTokenSetsRevokedFlag(
            @ForAll("userRoles") UserRole role
    ) {
        reset(refreshTokenRepository);

        String tokenValue = UUID.randomUUID().toString();
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(role)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token(tokenValue)
                .revoked(false)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(refreshToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        tokenService.revokeRefreshToken(tokenValue);

        assert refreshToken.isRevoked() : "Expected token to be revoked after revokeRefreshToken";
    }

    @Property
    void revokedRefreshTokenThrowsOnValidation(
            @ForAll("userRoles") UserRole role
    ) {
        reset(refreshTokenRepository);

        String tokenValue = UUID.randomUUID().toString();
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(role)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID())
                .user(user)
                .token(tokenValue)
                .revoked(true) // Already revoked
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        when(refreshTokenRepository.findByToken(tokenValue)).thenReturn(Optional.of(refreshToken));

        try {
            tokenService.validateRefreshToken(tokenValue);
            throw new AssertionError("Expected InvalidTokenException for revoked token");
        } catch (InvalidTokenException e) {
            assert e.getMessage().contains("revoked") : "Expected 'revoked' message but got: " + e.getMessage();
        }
    }

    @Property
    void generateRefreshTokenAlwaysProducesNewTokenValue(
            @ForAll("userRoles") UserRole role
    ) {
        reset(refreshTokenRepository);

        User user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(role)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        String token1 = tokenService.generateRefreshToken(user);
        String token2 = tokenService.generateRefreshToken(user);

        assert token1 != null : "First token should not be null";
        assert token2 != null : "Second token should not be null";
        assert !token1.equals(token2) : "Generated refresh tokens must be unique";
    }

    @Provide
    Arbitrary<UserRole> userRoles() {
        return Arbitraries.of(UserRole.ROLE_ATHLETE, UserRole.ROLE_COACH, UserRole.ROLE_ADMIN);
    }
}
