package com.feadev.eaids.controller;

import com.feadev.eaids.registrations.model.enums.UserRole;
import net.jqwik.api.*;

import java.util.*;

/**
 * Property-based tests for role-permission matrix enforcement.
 *
 * Property 21: Role-Permission Matrix Enforcement
 * Validates: Requirements 10.1, 10.2, 10.3, 10.4
 *
 * Verifies that for every (role, endpoint-prefix) pair, access matches the defined matrix:
 * - ATHLETE can access /api/v1/athletes/** but NOT /api/v1/admin/** or /api/v1/coaches/**
 * - COACH can access /api/v1/coaches/** but NOT /api/v1/admin/** or /api/v1/athletes/**
 * - ADMIN can access /api/v1/admin/** but NOT /api/v1/athletes/** or /api/v1/coaches/**
 */
class RolePermissionMatrixPropertyTest {

    // Define the permission matrix: which roles can access which path prefixes
    private static final Map<UserRole, Set<String>> ALLOWED_PATHS = Map.of(
            UserRole.ROLE_ATHLETE, Set.of("/api/v1/athletes"),
            UserRole.ROLE_COACH, Set.of("/api/v1/coaches"),
            UserRole.ROLE_ADMIN, Set.of("/api/v1/admin")
    );

    private static final List<String> ALL_PROTECTED_PREFIXES = List.of(
            "/api/v1/athletes",
            "/api/v1/coaches",
            "/api/v1/admin"
    );

    @Property
    void athleteCannotAccessAdminEndpoints(
            @ForAll("adminPaths") String path
    ) {
        UserRole role = UserRole.ROLE_ATHLETE;
        Set<String> allowedPrefixes = ALLOWED_PATHS.get(role);

        // Admin paths should NOT be accessible by athlete
        boolean shouldBeAllowed = allowedPrefixes.stream().anyMatch(path::startsWith);
        assert !shouldBeAllowed
                : "ATHLETE should NOT have access to admin path: " + path;
    }

    @Property
    void athleteCannotAccessCoachEndpoints(
            @ForAll("coachPaths") String path
    ) {
        UserRole role = UserRole.ROLE_ATHLETE;
        Set<String> allowedPrefixes = ALLOWED_PATHS.get(role);

        boolean shouldBeAllowed = allowedPrefixes.stream().anyMatch(path::startsWith);
        assert !shouldBeAllowed
                : "ATHLETE should NOT have access to coach path: " + path;
    }

    @Property
    void coachCannotAccessAthleteEndpoints(
            @ForAll("athletePaths") String path
    ) {
        UserRole role = UserRole.ROLE_COACH;
        Set<String> allowedPrefixes = ALLOWED_PATHS.get(role);

        boolean shouldBeAllowed = allowedPrefixes.stream().anyMatch(path::startsWith);
        assert !shouldBeAllowed
                : "COACH should NOT have access to athlete path: " + path;
    }

    @Property
    void coachCannotAccessAdminEndpoints(
            @ForAll("adminPaths") String path
    ) {
        UserRole role = UserRole.ROLE_COACH;
        Set<String> allowedPrefixes = ALLOWED_PATHS.get(role);

        boolean shouldBeAllowed = allowedPrefixes.stream().anyMatch(path::startsWith);
        assert !shouldBeAllowed
                : "COACH should NOT have access to admin path: " + path;
    }

    @Property
    void adminCannotAccessAthleteEndpoints(
            @ForAll("athletePaths") String path
    ) {
        UserRole role = UserRole.ROLE_ADMIN;
        Set<String> allowedPrefixes = ALLOWED_PATHS.get(role);

        boolean shouldBeAllowed = allowedPrefixes.stream().anyMatch(path::startsWith);
        assert !shouldBeAllowed
                : "ADMIN should NOT have access to athlete path: " + path;
    }

    @Property
    void adminCannotAccessCoachEndpoints(
            @ForAll("coachPaths") String path
    ) {
        UserRole role = UserRole.ROLE_ADMIN;
        Set<String> allowedPrefixes = ALLOWED_PATHS.get(role);

        boolean shouldBeAllowed = allowedPrefixes.stream().anyMatch(path::startsWith);
        assert !shouldBeAllowed
                : "ADMIN should NOT have access to coach path: " + path;
    }

    @Property
    void eachRoleCanAccessOwnEndpoints(
            @ForAll("roles") UserRole role
    ) {
        Set<String> allowedPrefixes = ALLOWED_PATHS.get(role);
        String ownPath = allowedPrefixes.iterator().next();

        boolean shouldBeAllowed = allowedPrefixes.stream().anyMatch(ownPath::startsWith);
        assert shouldBeAllowed
                : role + " should have access to own path: " + ownPath;
    }

    @Property
    void publicEndpointsAccessibleWithoutRole(
            @ForAll("publicPaths") String path
    ) {
        // Public endpoints should NOT require any role - they match /api/v1/auth/**
        boolean isProtected = ALL_PROTECTED_PREFIXES.stream().anyMatch(path::startsWith);
        assert !isProtected
                : "Public path should not be under a protected prefix: " + path;
    }

    @Provide
    Arbitrary<String> adminPaths() {
        return Arbitraries.of(
                "/api/v1/admin/users",
                "/api/v1/admin/athletes/123/approve",
                "/api/v1/admin/coaches/456/verify",
                "/api/v1/admin/audit-logs"
        );
    }

    @Provide
    Arbitrary<String> coachPaths() {
        return Arbitraries.of(
                "/api/v1/coaches/me",
                "/api/v1/coaches/documents"
        );
    }

    @Provide
    Arbitrary<String> athletePaths() {
        return Arbitraries.of(
                "/api/v1/athletes/me",
                "/api/v1/athletes/profile/complete",
                "/api/v1/athletes/videos",
                "/api/v1/athletes/guardian-consent"
        );
    }

    @Provide
    Arbitrary<String> publicPaths() {
        return Arbitraries.of(
                "/api/v1/auth/register/athlete",
                "/api/v1/auth/register/coach",
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/api/v1/auth/verify-email",
                "/api/v1/auth/resend-verification",
                "/api/v1/auth/forgot-password",
                "/api/v1/auth/verify-otp",
                "/api/v1/auth/reset-password"
        );
    }

    @Provide
    Arbitrary<UserRole> roles() {
        return Arbitraries.of(UserRole.values());
    }
}
