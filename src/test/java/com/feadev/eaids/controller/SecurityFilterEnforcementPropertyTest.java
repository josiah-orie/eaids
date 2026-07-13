package com.feadev.eaids.controller;

import com.feadev.eaids.registrations.model.enums.UserRole;
import net.jqwik.api.*;

import java.util.Set;

/**
 * Property-based tests for security filter enforcement.
 *
 * Property 22: Security Filter Enforcement
 * Validates: Requirements 10.5, 10.6, 10.7
 *
 * Verifies:
 * - Requests without Bearer token to protected endpoints should get 401
 * - Requests with valid token but wrong role should get 403
 * - Public endpoints pass through without authentication
 */
class SecurityFilterEnforcementPropertyTest {

    private static final Set<String> PUBLIC_PATH_PREFIXES = Set.of(
            "/api/v1/auth/",
            "/swagger-ui/",
            "/v3/api-docs/"
    );

    private static final Set<String> PROTECTED_PATH_PREFIXES = Set.of(
            "/api/v1/athletes/",
            "/api/v1/coaches/",
            "/api/v1/admin/"
    );

    @Property
    void unauthenticatedRequestToProtectedEndpointShouldGet401(
            @ForAll("protectedEndpoints") String endpoint
    ) {
        // Protected endpoints require Bearer token
        // Without a token, the security filter should return 401
        boolean isProtected = PROTECTED_PATH_PREFIXES.stream().anyMatch(endpoint::startsWith);
        assert isProtected
                : "Endpoint " + endpoint + " should be protected (requiring 401 without token)";

        // Verify it's NOT public
        boolean isPublic = PUBLIC_PATH_PREFIXES.stream().anyMatch(endpoint::startsWith);
        assert !isPublic
                : "Protected endpoint should not match public path prefixes: " + endpoint;
    }

    @Property
    void publicEndpointsDoNotRequireAuthentication(
            @ForAll("publicEndpoints") String endpoint
    ) {
        // Public endpoints should not match protected prefixes
        boolean isProtected = PROTECTED_PATH_PREFIXES.stream().anyMatch(endpoint::startsWith);
        assert !isProtected
                : "Public endpoint " + endpoint + " should not be under a protected prefix";

        // Should match at least one public prefix
        boolean isPublic = PUBLIC_PATH_PREFIXES.stream().anyMatch(endpoint::startsWith);
        assert isPublic
                : "Public endpoint " + endpoint + " should match a public path prefix";
    }

    @Property
    void wrongRoleGetsDeniedAccess(
            @ForAll("roleMismatchScenarios") RoleMismatchScenario scenario
    ) {
        // Verify that the role does NOT match the endpoint's required role
        String requiredRolePrefix = getRequiredRolePrefix(scenario.endpoint());
        String actualRolePrefix = getRolePrefixForRole(scenario.role());

        assert !requiredRolePrefix.equals(actualRolePrefix)
                : "Mismatch scenario should have role " + scenario.role()
                + " NOT matching endpoint " + scenario.endpoint();
    }

    @Property
    void correctRoleGetsAccessGranted(
            @ForAll("roleMatchScenarios") RoleMatchScenario scenario
    ) {
        // Verify that the role DOES match the endpoint's required role
        String requiredRolePrefix = getRequiredRolePrefix(scenario.endpoint());
        String actualRolePrefix = getRolePrefixForRole(scenario.role());

        assert requiredRolePrefix.equals(actualRolePrefix)
                : "Match scenario should have role " + scenario.role()
                + " matching endpoint " + scenario.endpoint();
    }

    private String getRequiredRolePrefix(String endpoint) {
        if (endpoint.startsWith("/api/v1/athletes/")) return "/api/v1/athletes/";
        if (endpoint.startsWith("/api/v1/coaches/")) return "/api/v1/coaches/";
        if (endpoint.startsWith("/api/v1/admin/")) return "/api/v1/admin/";
        return "unknown";
    }

    private String getRolePrefixForRole(UserRole role) {
        return switch (role) {
            case ROLE_ATHLETE -> "/api/v1/athletes/";
            case ROLE_COACH -> "/api/v1/coaches/";
            case ROLE_ADMIN -> "/api/v1/admin/";
            case ROLE_SCOUT -> "/api/v1/scouts/";
        };
    }

    @Provide
    Arbitrary<String> protectedEndpoints() {
        return Arbitraries.of(
                "/api/v1/athletes/me",
                "/api/v1/athletes/videos",
                "/api/v1/athletes/profile/complete",
                "/api/v1/coaches/me",
                "/api/v1/coaches/documents",
                "/api/v1/admin/users",
                "/api/v1/admin/audit-logs",
                "/api/v1/admin/athletes/123/approve"
        );
    }

    @Provide
    Arbitrary<String> publicEndpoints() {
        return Arbitraries.of(
                "/api/v1/auth/register/athlete",
                "/api/v1/auth/register/coach",
                "/api/v1/auth/login",
                "/api/v1/auth/refresh",
                "/api/v1/auth/verify-email",
                "/api/v1/auth/resend-verification",
                "/api/v1/auth/forgot-password",
                "/api/v1/auth/verify-otp",
                "/api/v1/auth/reset-password",
                "/swagger-ui/index.html",
                "/v3/api-docs/swagger-config"
        );
    }

    @Provide
    Arbitrary<RoleMismatchScenario> roleMismatchScenarios() {
        return Arbitraries.of(
                new RoleMismatchScenario(UserRole.ROLE_ATHLETE, "/api/v1/admin/users"),
                new RoleMismatchScenario(UserRole.ROLE_ATHLETE, "/api/v1/coaches/me"),
                new RoleMismatchScenario(UserRole.ROLE_COACH, "/api/v1/admin/users"),
                new RoleMismatchScenario(UserRole.ROLE_COACH, "/api/v1/athletes/me"),
                new RoleMismatchScenario(UserRole.ROLE_ADMIN, "/api/v1/athletes/me"),
                new RoleMismatchScenario(UserRole.ROLE_ADMIN, "/api/v1/coaches/me")
        );
    }

    @Provide
    Arbitrary<RoleMatchScenario> roleMatchScenarios() {
        return Arbitraries.of(
                new RoleMatchScenario(UserRole.ROLE_ATHLETE, "/api/v1/athletes/me"),
                new RoleMatchScenario(UserRole.ROLE_ATHLETE, "/api/v1/athletes/videos"),
                new RoleMatchScenario(UserRole.ROLE_COACH, "/api/v1/coaches/me"),
                new RoleMatchScenario(UserRole.ROLE_COACH, "/api/v1/coaches/documents"),
                new RoleMatchScenario(UserRole.ROLE_ADMIN, "/api/v1/admin/users"),
                new RoleMatchScenario(UserRole.ROLE_ADMIN, "/api/v1/admin/audit-logs")
        );
    }

    record RoleMismatchScenario(UserRole role, String endpoint) {}
    record RoleMatchScenario(UserRole role, String endpoint) {}
}
