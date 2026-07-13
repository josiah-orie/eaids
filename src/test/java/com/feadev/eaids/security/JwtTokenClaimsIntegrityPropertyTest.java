package com.feadev.eaids.security;

import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.model.enums.UserStatus;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import net.jqwik.api.*;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.UUID;

/**
 * Property-based tests for JWT token claims integrity.
 *
 * Property 17: JWT Token Claims Integrity
 * Validates: Requirements 8.5
 */
class JwtTokenClaimsIntegrityPropertyTest {

    // Generate a valid HMAC-SHA256 key (at least 256 bits / 32 bytes)
    private static final String SECRET = Base64.getEncoder()
            .encodeToString("test-secret-key-for-jwt-property-tests-must-be-at-least-256-bits".getBytes());
    private static final long EXPIRATION_SECONDS = 3600;
    private static final SecretKey SIGNING_KEY = Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(SECRET, EXPIRATION_SECONDS);

    @Property
    void generatedTokenAlwaysContainsCorrectUserId(
            @ForAll("userRoles") UserRole role,
            @ForAll("userStatuses") UserStatus status,
            @ForAll("validEmails") String email
    ) {
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .email(email)
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(role)
                .status(status)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        String token = jwtTokenProvider.generateToken(user);
        UUID extractedUserId = jwtTokenProvider.validateToken(token);

        assert extractedUserId.equals(userId)
                : "Expected userId " + userId + " but got " + extractedUserId;
    }

    @Property
    void generatedTokenAlwaysContainsCorrectEmail(
            @ForAll("userRoles") UserRole role,
            @ForAll("userStatuses") UserStatus status,
            @ForAll("validEmails") String email
    ) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(role)
                .status(status)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        String token = jwtTokenProvider.generateToken(user);
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);

        assert extractedEmail.equals(email)
                : "Expected email '" + email + "' but got '" + extractedEmail + "'";
    }

    @Property
    void generatedTokenAlwaysContainsCorrectRole(
            @ForAll("userRoles") UserRole role,
            @ForAll("userStatuses") UserStatus status
    ) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(role)
                .status(status)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        String token = jwtTokenProvider.generateToken(user);
        String extractedRole = jwtTokenProvider.getRoleFromToken(token);

        assert extractedRole.equals(role.name())
                : "Expected role '" + role.name() + "' but got '" + extractedRole + "'";
    }

    @Property
    void generatedTokenAlwaysContainsCorrectStatus(
            @ForAll("userRoles") UserRole role,
            @ForAll("userStatuses") UserStatus status
    ) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .phoneNumber("+2348012345678")
                .password("encoded")
                .role(role)
                .status(status)
                .emailVerified(true)
                .phoneVerified(false)
                .enabled(true)
                .build();

        String token = jwtTokenProvider.generateToken(user);

        // Parse the token directly to check the status claim
        Claims claims = Jwts.parser()
                .verifyWith(SIGNING_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        String extractedStatus = claims.get("status", String.class);
        assert extractedStatus.equals(status.name())
                : "Expected status '" + status.name() + "' but got '" + extractedStatus + "'";
    }

    @Provide
    Arbitrary<UserRole> userRoles() {
        return Arbitraries.of(UserRole.ROLE_ATHLETE, UserRole.ROLE_COACH, UserRole.ROLE_ADMIN);
    }

    @Provide
    Arbitrary<UserStatus> userStatuses() {
        return Arbitraries.of(UserStatus.values());
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
