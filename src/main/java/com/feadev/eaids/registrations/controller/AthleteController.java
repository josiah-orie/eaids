package com.feadev.eaids.registrations.controller;

import com.feadev.eaids.registrations.dto.request.AthleteProfileCompletionRequest;
import com.feadev.eaids.registrations.dto.request.AthleteProfileUpdateRequest;
import com.feadev.eaids.registrations.dto.request.GuardianConsentRequest;
import com.feadev.eaids.registrations.dto.request.VideoAddRequest;
import com.feadev.eaids.registrations.dto.response.AthleteProfileResponse;
import com.feadev.eaids.security.CustomUserDetails;
import com.feadev.eaids.registrations.service.AthleteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/athletes")
@RequiredArgsConstructor
@Tag(name = "Athletes")
public class AthleteController {

    private final AthleteService athleteService;

    @PostMapping("/profile/complete")
    @Operation(summary = "Complete athlete profile", description = "Complete profile with personal, physical, and sports information after email verification")
    public ResponseEntity<AthleteProfileResponse> completeProfile(
            @Valid @RequestBody AthleteProfileCompletionRequest request) {
        UUID userId = getCurrentUserId();
        AthleteProfileResponse response = athleteService.completeProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/guardian-consent")
    @Operation(summary = "Submit guardian consent", description = "Submit guardian consent for minor athletes (under 18)")
    public ResponseEntity<AthleteProfileResponse> submitGuardianConsent(
            @Valid @RequestBody GuardianConsentRequest request) {
        UUID userId = getCurrentUserId();
        AthleteProfileResponse response = athleteService.submitGuardianConsent(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get athlete profile", description = "Retrieve the authenticated athlete's complete profile")
    public ResponseEntity<AthleteProfileResponse> getProfile() {
        UUID userId = getCurrentUserId();
        AthleteProfileResponse response = athleteService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "Update athlete profile", description = "Update the authenticated athlete's profile fields")
    public ResponseEntity<AthleteProfileResponse> updateProfile(
            @Valid @RequestBody AthleteProfileUpdateRequest request) {
        UUID userId = getCurrentUserId();
        AthleteProfileResponse response = athleteService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/videos")
    @Operation(summary = "Add video link", description = "Add a new video link to the athlete's profile")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AthleteProfileResponse.VideoResponse> addVideo(
            @Valid @RequestBody VideoAddRequest request) {
        UUID userId = getCurrentUserId();
        AthleteProfileResponse.VideoResponse response = athleteService.addVideo(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/videos/{id}")
    @Operation(summary = "Delete video link", description = "Remove a video link from the athlete's profile")
    public ResponseEntity<Void> deleteVideo(@PathVariable UUID id) {
        UUID userId = getCurrentUserId();
        athleteService.deleteVideo(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/me/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile image", description = "Upload a full-size profile image for the athlete. Allowed types: JPG, JPEG, PNG. Max size: 5MB.")
    public ResponseEntity<AthleteProfileResponse> uploadProfileImage(
            @RequestParam("file") MultipartFile file) {
        UUID userId = getCurrentUserId();
        AthleteProfileResponse response = athleteService.uploadProfileImage(userId, file);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/me/icon-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload icon/passport image", description = "Upload a small icon or passport-sized image for the athlete. Allowed types: JPG, JPEG, PNG. Max size: 5MB.")
    public ResponseEntity<AthleteProfileResponse> uploadIconImage(
            @RequestParam("file") MultipartFile file) {
        UUID userId = getCurrentUserId();
        AthleteProfileResponse response = athleteService.uploadIconImage(userId, file);
        return ResponseEntity.ok(response);
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }
}
