package com.feadev.eaids.scouting.controller;

import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.scouting.dto.request.CreateScoutProfileRequest;
import com.feadev.eaids.scouting.dto.response.ScoutProfileResponse;
import com.feadev.eaids.scouting.service.ScoutProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scouts/me/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SCOUT')")
@Tag(name = "Scout Profile", description = "Endpoints for scout profile completion and retrieval")
public class ScoutProfileController {

    private final ScoutProfileService scoutProfileService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Complete scout profile", description = "Creates the scout profile after initial password setup. Required before accessing scouting features.")
    public ResponseEntity<ScoutProfileResponse> createProfile(
            @Valid @RequestBody CreateScoutProfileRequest request) {
        UUID userId = getCurrentUserId();
        ScoutProfileResponse response = scoutProfileService.createProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get scout profile", description = "Returns the current scout's profile information")
    public ResponseEntity<ScoutProfileResponse> getProfile() {
        UUID userId = getCurrentUserId();
        ScoutProfileResponse response = scoutProfileService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    private UUID getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        return user.getId();
    }
}
