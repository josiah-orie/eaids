package com.feadev.eaids.positionprofile.controller;

import com.feadev.eaids.positionprofile.dto.request.CreatePositionProfileRequest;
import com.feadev.eaids.positionprofile.dto.request.UpdatePositionProfileRequest;
import com.feadev.eaids.positionprofile.dto.response.PositionProfileResponse;
import com.feadev.eaids.positionprofile.service.PositionProfileService;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/position-profiles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Position Profiles", description = "Global position profile management with metrics and weights")
public class PositionProfileController {

    private final PositionProfileService positionProfileService;

    @PostMapping
    @Operation(summary = "Create a position profile", description = "Creates a new position profile with scoring metrics and weight template")
    @ApiResponse(responseCode = "201", description = "Profile created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request or active profile already exists for position")
    public ResponseEntity<PositionProfileResponse> createProfile(
            @Valid @RequestBody CreatePositionProfileRequest request) {
        PositionProfileResponse response = positionProfileService.createProfile(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List position profiles", description = "Lists all position profiles with optional filtering by position and active status")
    @ApiResponse(responseCode = "200", description = "Profiles retrieved successfully")
    public ResponseEntity<List<PositionProfileResponse>> getAllProfiles(
            @RequestParam(required = false) FootballPosition position,
            @RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(positionProfileService.getAllProfiles(position, active));
    }

    @GetMapping("/{profileId}")
    @Operation(summary = "Get position profile by ID", description = "Retrieves a position profile with nested metrics and weight template")
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Profile not found")
    public ResponseEntity<PositionProfileResponse> getProfile(@PathVariable UUID profileId) {
        return ResponseEntity.ok(positionProfileService.getProfile(profileId));
    }

    @PutMapping("/{profileId}")
    @Operation(summary = "Update a position profile", description = "Updates profile name, description, metrics, and/or weight template")
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "404", description = "Profile not found")
    public ResponseEntity<PositionProfileResponse> updateProfile(
            @PathVariable UUID profileId,
            @Valid @RequestBody UpdatePositionProfileRequest request) {
        return ResponseEntity.ok(positionProfileService.updateProfile(profileId, request));
    }

    @DeleteMapping("/{profileId}")
    @Operation(summary = "Deactivate a position profile", description = "Soft-deletes a position profile by setting isActive to false")
    @ApiResponse(responseCode = "204", description = "Profile deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Profile not found")
    public ResponseEntity<Void> deleteProfile(@PathVariable UUID profileId) {
        positionProfileService.deleteProfile(profileId);
        return ResponseEntity.noContent().build();
    }
}
