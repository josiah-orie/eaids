package com.feadev.eaids.positionprofile.controller;

import com.feadev.eaids.positionprofile.dto.request.AssignPositionProfileRequest;
import com.feadev.eaids.positionprofile.dto.response.CampPositionProfileResponse;
import com.feadev.eaids.positionprofile.service.CampPositionProfileService;
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
@RequestMapping("/api/v1/camps/{campId}/position-profiles")
@RequiredArgsConstructor
@Tag(name = "Camp Position Profiles", description = "Assign and manage position profiles for camps")
public class CampPositionProfileController {

    private final CampPositionProfileService campPositionProfileService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Assign a position profile to a camp", description = "Links a global position profile to a camp for scoring weight propagation")
    @ApiResponse(responseCode = "201", description = "Profile assigned successfully")
    @ApiResponse(responseCode = "400", description = "Profile already assigned or inactive")
    @ApiResponse(responseCode = "404", description = "Camp or profile not found")
    public ResponseEntity<CampPositionProfileResponse> assignProfile(
            @PathVariable UUID campId,
            @Valid @RequestBody AssignPositionProfileRequest request) {
        CampPositionProfileResponse response = campPositionProfileService.assignProfile(campId, request.getPositionProfileId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List position profiles assigned to a camp", description = "Returns all position profiles linked to the specified camp")
    @ApiResponse(responseCode = "200", description = "Profiles retrieved successfully")
    public ResponseEntity<List<CampPositionProfileResponse>> getCampProfiles(@PathVariable UUID campId) {
        return ResponseEntity.ok(campPositionProfileService.getCampProfiles(campId));
    }

    @DeleteMapping("/{profileId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Unassign a position profile from a camp", description = "Removes the link between a position profile and a camp")
    @ApiResponse(responseCode = "204", description = "Profile unassigned successfully")
    @ApiResponse(responseCode = "404", description = "Assignment not found")
    public ResponseEntity<Void> unassignProfile(
            @PathVariable UUID campId,
            @PathVariable UUID profileId) {
        campPositionProfileService.unassignProfile(campId, profileId);
        return ResponseEntity.noContent().build();
    }
}
