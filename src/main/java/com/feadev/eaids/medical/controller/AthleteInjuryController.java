package com.feadev.eaids.medical.controller;

import com.feadev.eaids.medical.dto.request.ReportInjuryRequest;
import com.feadev.eaids.medical.dto.request.UpdateInjuryRequest;
import com.feadev.eaids.medical.model.enums.InjuryStatus;
import com.feadev.eaids.medical.service.AthleteInjuryService;
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

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Athlete Injuries", description = "Injury reporting, management, and status workflow")
public class AthleteInjuryController {

    private final AthleteInjuryService injuryService;

    @PostMapping("/api/v1/athletes/{athleteId}/injuries")
    @Operation(summary = "Report an injury for an athlete",
               description = "Any authenticated user may report an injury. " +
                             "If isSuspectedConcussion is true, an urgent alert is sent to medical staff.")
    public ResponseEntity<Object> reportInjury(
            @PathVariable UUID athleteId,
            @Valid @RequestBody ReportInjuryRequest request,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(injuryService.reportInjury(athleteId, request,
                        actor.getId(), actor.getRole()));
    }

    @GetMapping("/api/v1/athletes/{athleteId}/injuries")
    @Operation(summary = "List injuries for an athlete",
               description = "Athletes see their own injuries (no clinical fields). " +
                             "Medical officers, physiotherapists, and admins see full details.")
    public ResponseEntity<Page<?>> getInjuriesForAthlete(
            @PathVariable UUID athleteId,
            Pageable pageable,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                injuryService.getInjuriesForAthlete(athleteId, actor.getId(),
                        actor.getRole(), pageable));
    }

    @GetMapping("/api/v1/injuries/{injuryId}")
    @Operation(summary = "Get an injury record by ID")
    public ResponseEntity<Object> getInjury(
            @PathVariable UUID injuryId,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                injuryService.getInjury(injuryId, actor.getId(), actor.getRole()));
    }

    @PutMapping("/api/v1/injuries/{injuryId}")
    @Operation(summary = "Update an injury record",
               description = "Clinical fields (severity, diagnosis) require MEDICAL_OFFICER, " +
                             "PHYSIOTHERAPIST, or ADMIN role.")
    public ResponseEntity<Object> updateInjury(
            @PathVariable UUID injuryId,
            @RequestBody UpdateInjuryRequest request,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                injuryService.updateInjury(injuryId, request,
                        actor.getId(), actor.getRole()));
    }

    @PatchMapping("/api/v1/injuries/{injuryId}/status")
    @PreAuthorize("hasAnyRole('MEDICAL_OFFICER','ADMIN','PHYSIOTHERAPIST')")
    @Operation(summary = "Transition injury status",
               description = "REPORTED → AWAITING_ASSESSMENT → UNDER_TREATMENT → REHABILITATION " +
                             "→ AWAITING_CLEARANCE → RESOLVED → CLOSED. " +
                             "RESOLVED and CLOSED require MEDICAL_OFFICER or ADMIN.")
    public ResponseEntity<Object> transitionStatus(
            @PathVariable UUID injuryId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        InjuryStatus newStatus = InjuryStatus.valueOf(body.get("status").toUpperCase());
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                injuryService.transitionStatus(injuryId, newStatus,
                        actor.getId(), actor.getRole()));
    }
}
