package com.feadev.eaids.medical.controller;

import com.feadev.eaids.medical.dto.request.IssueClearanceRequest;
import com.feadev.eaids.medical.dto.request.RevokeClearanceRequest;
import com.feadev.eaids.medical.dto.response.MedicalClearanceResponse;
import com.feadev.eaids.medical.dto.response.ParticipationEligibilityResponse;
import com.feadev.eaids.medical.service.MedicalClearanceService;
import com.feadev.eaids.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Medical Clearance",
     description = "Issue, revoke, and check medical clearance for athlete participation")
public class MedicalClearanceController {

    private final MedicalClearanceService clearanceService;

    @PostMapping("/api/v1/athletes/{athleteId}/medical-clearances")
    @PreAuthorize("hasAnyRole('MEDICAL_OFFICER','ADMIN')")
    @Operation(summary = "Issue a medical clearance for an athlete",
               description = "Only medical officers and admins may issue clearances. " +
                             "issuedBy is always set from the authenticated principal.")
    public ResponseEntity<MedicalClearanceResponse> issueClearance(
            @PathVariable UUID athleteId,
            @Valid @RequestBody IssueClearanceRequest request,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clearanceService.issueClearance(athleteId, request,
                        actor.getId(), actor.getRole()));
    }

    @GetMapping("/api/v1/athletes/{athleteId}/medical-clearances")
    @Operation(summary = "List clearances for an athlete",
               description = "Athletes see their own full clearance list. " +
                             "Coaches and scouts should use the participation-eligibility endpoint instead.")
    public ResponseEntity<Page<MedicalClearanceResponse>> getClearancesForAthlete(
            @PathVariable UUID athleteId,
            Pageable pageable,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                clearanceService.getClearancesForAthlete(athleteId, actor.getId(),
                        actor.getRole(), pageable));
    }

    @GetMapping("/api/v1/medical-clearances/{clearanceId}")
    @Operation(summary = "Get a medical clearance by ID",
               description = "Full details for MO/admin/athlete-own. " +
                             "Coaches and scouts receive operational view only.")
    public ResponseEntity<Object> getClearance(
            @PathVariable UUID clearanceId,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                clearanceService.getClearance(clearanceId, actor.getId(), actor.getRole()));
    }

    @PostMapping("/api/v1/medical-clearances/{clearanceId}/revoke")
    @PreAuthorize("hasAnyRole('MEDICAL_OFFICER','ADMIN')")
    @Operation(summary = "Revoke a medical clearance",
               description = "Only medical officers and admins may revoke clearances.")
    public ResponseEntity<MedicalClearanceResponse> revokeClearance(
            @PathVariable UUID clearanceId,
            @Valid @RequestBody RevokeClearanceRequest request,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                clearanceService.revokeClearance(clearanceId, request,
                        actor.getId(), actor.getRole()));
    }

    @GetMapping("/api/v1/athletes/{athleteId}/participation-eligibility")
    @Operation(summary = "Check an athlete's participation eligibility",
               description = "Returns eligibility status and operational restrictions. " +
                             "Does NOT expose diagnoses, clinical notes, or full clearance history. " +
                             "Accessible by all authenticated users.")
    public ResponseEntity<ParticipationEligibilityResponse> getParticipationEligibility(
            @PathVariable UUID athleteId,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                clearanceService.getParticipationEligibility(athleteId,
                        actor.getId(), actor.getRole()));
    }
}
