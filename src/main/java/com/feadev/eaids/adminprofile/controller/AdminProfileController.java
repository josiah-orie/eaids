package com.feadev.eaids.adminprofile.controller;

import com.feadev.eaids.adminprofile.dto.request.CreateAdminProfileRequest;
import com.feadev.eaids.adminprofile.dto.request.UpdateAdminProfileRequest;
import com.feadev.eaids.adminprofile.dto.response.AdminProfileResponse;
import com.feadev.eaids.adminprofile.service.AdminProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/profile")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Profiles", description = "Admin profile management endpoints")
public class AdminProfileController {

    private final AdminProfileService adminProfileService;

    @GetMapping
    @Operation(summary = "Get own admin profile", description = "Retrieves the authenticated admin's profile")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Profile not found")
    public ResponseEntity<AdminProfileResponse> getOwnProfile(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(adminProfileService.getOwnProfile(userId));
    }

    @PostMapping
    @Operation(summary = "Create own admin profile", description = "Creates a profile for the authenticated admin")
    @ApiResponse(responseCode = "201", description = "Profile created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or profile already exists")
    public ResponseEntity<AdminProfileResponse> createProfile(
            Authentication authentication,
            @Valid @RequestBody CreateAdminProfileRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        AdminProfileResponse response = adminProfileService.createProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping
    @Operation(summary = "Update own admin profile", description = "Updates the authenticated admin's profile")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "404", description = "Profile not found")
    public ResponseEntity<AdminProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateAdminProfileRequest request) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(adminProfileService.updateProfile(userId, request));
    }

    @GetMapping("/{profileId}")
    @Operation(summary = "Get admin profile by ID", description = "Retrieves another admin's profile by profile ID")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Profile not found")
    public ResponseEntity<AdminProfileResponse> getProfileById(@PathVariable UUID profileId) {
        return ResponseEntity.ok(adminProfileService.getProfileById(profileId));
    }
}
