package com.feadev.eaids.medical.controller;

import com.feadev.eaids.medical.dto.request.UpdateMedicalProfileRequest;
import com.feadev.eaids.medical.dto.response.MedicalProfileResponse;
import com.feadev.eaids.medical.service.MedicalProfileService;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/athletes/{athleteId}/medical-profile")
@RequiredArgsConstructor
@Tag(name = "Medical Profile", description = "Athlete medical history and health questionnaire")
public class MedicalProfileController {

    private final MedicalProfileService medicalProfileService;

    @GetMapping
    @Operation(summary = "Get athlete medical profile",
               description = "Athletes may only view their own profile. Medical officers, " +
                             "physiotherapists, and admins may view any athlete's profile.")
    public ResponseEntity<MedicalProfileResponse> getProfile(
            @PathVariable UUID athleteId,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                medicalProfileService.getProfile(athleteId, actor.getId(), actor.getRole()));
    }

    @PutMapping
    @Operation(summary = "Create or update athlete medical profile",
               description = "Athletes may only update their own profile. " +
                             "Medical officers and admins may update any athlete's profile.")
    public ResponseEntity<MedicalProfileResponse> createOrUpdateProfile(
            @PathVariable UUID athleteId,
            @Valid @RequestBody UpdateMedicalProfileRequest request,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                medicalProfileService.createOrUpdateProfile(athleteId, request,
                        actor.getId(), actor.getRole()));
    }
}
