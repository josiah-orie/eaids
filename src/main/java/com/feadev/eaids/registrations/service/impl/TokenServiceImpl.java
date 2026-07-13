package com.feadev.eaids.registrations.service.impl;

import com.feadev.eaids.config.JwtConfig;
import com.feadev.eaids.registrations.exception.InvalidTokenException;
import com.feadev.eaids.registrations.model.RefreshToken;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.repository.RefreshTokenRepository;
import com.feadev.eaids.security.JwtTokenProvider;
import com.feadev.eaids.registrations.service.TokenService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenServiceImpl implements TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;

    @Override
    public String generateAccessToken(User user) {
        return jwtTokenProvider.generateToken(user);
    }

    @Override
    @Transactional
    public String generateRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtConfig.getRefreshTokenExpiration());

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .revoked(false)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    @Override
    public UUID validateAccessToken(String token) {
        try {
            return jwtTokenProvider.validateToken(token);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Invalid or expired access token");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (refreshToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidTokenException("Refresh token has expired");
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public void revokeAllUserRefreshTokens(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
