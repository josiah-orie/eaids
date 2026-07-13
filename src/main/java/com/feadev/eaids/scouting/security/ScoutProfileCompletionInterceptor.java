package com.feadev.eaids.scouting.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.scouting.repository.ScoutProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Interceptor that blocks scouts from accessing scouting endpoints
 * until they have completed their profile.
 * <p>
 * Allows through: POST/GET to /api/v1/scouts/me/profile (profile completion itself).
 * Blocks: all other /api/v1/scouts/** and /api/v1/scouting/** requests if no ScoutProfile exists.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScoutProfileCompletionInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    private final ScoutProfileRepository scoutProfileRepository;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();

        // Allow profile completion endpoint itself
        if (requestUri.equals("/api/v1/scouts/me/profile")) {
            return true;
        }

        // Only apply to scout-protected paths
        if (!requestUri.startsWith("/api/v1/scouts/") && !requestUri.startsWith("/api/v1/scouting/")) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return true; // Let Spring Security handle unauthenticated requests
        }

        String email = authentication.getName();
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return true; // Let downstream handle missing user
        }

        User user = userOptional.get();

        // Only enforce for scouts
        if (user.getRole() != UserRole.ROLE_SCOUT) {
            return true;
        }

        // Check if scout profile exists
        if (scoutProfileRepository.existsByUserId(user.getId())) {
            return true; // Profile exists, allow through
        }

        // Block access — profile not completed
        log.debug("Scout {} attempted to access {} without completed profile", email, requestUri);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        Map<String, Object> body = Map.of(
                "timestamp", Instant.now().toString(),
                "status", 403,
                "message", "Scout profile completion required before accessing scouting features",
                "profileUrl", "/api/v1/scouts/me/profile"
        );

        objectMapper.writeValue(response.getOutputStream(), body);
        return false;
    }
}
