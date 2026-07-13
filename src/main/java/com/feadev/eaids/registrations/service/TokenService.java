package com.feadev.eaids.registrations.service;

import com.feadev.eaids.registrations.model.RefreshToken;
import com.feadev.eaids.registrations.model.User;

import java.util.UUID;

public interface TokenService {

    String generateAccessToken(User user);

    String generateRefreshToken(User user);

    UUID validateAccessToken(String token);

    RefreshToken validateRefreshToken(String token);

    void revokeRefreshToken(String token);

    void revokeAllUserRefreshTokens(UUID userId);
}
