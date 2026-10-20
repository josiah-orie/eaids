package com.feadev.eaids.medical.controller;

import com.feadev.eaids.medical.dto.response.InjuryRiskResponse;
import com.feadev.eaids.medical.service.InjuryRiskService;
import com.feadev.eaids.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController("medicalInjuryRiskController")
@RequiredArgsConstructor
@Tag(name = "Injury Risk Prediction", description = "Per-athlete heuristic injury risk scores with explanatory flags")
public class InjuryRiskController {

    private final InjuryRiskService injuryRiskService;

    @GetMapping("/api/v1/athletes/{athleteId}/injury-risk")
    @PreAuthorize("hasAnyRole('MEDICAL_OFFICER','ADMIN','PHYSIOTHERAPIST') " +
                  "or #athleteId == authentication.principal.id")
    @Operation(
        summary = "Get injury risk profile for an athlete",
        description = "Returns the cached heuristic risk score (LOW / MEDIUM / HIGH / CRITICAL) " +
                      "with explanatory flags and a plain-English summary. " +
                      "Accessible by MEDICAL_OFFICER, ADMIN, PHYSIOTHERAPIST, or the athlete themselves."
    )
    @ApiResponse(responseCode = "200", description = "Risk profile returned")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    public ResponseEntity<InjuryRiskResponse> getRiskProfile(
            @PathVariable UUID athleteId,
            Authentication authentication) {
        return ResponseEntity.ok(injuryRiskService.getRiskProfile(athleteId));
    }

    @DeleteMapping("/api/v1/admin/athletes/{athleteId}/injury-risk/cache")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Manually evict the injury risk cache for an athlete",
        description = "Forces recomputation on the next GET call. " +
                      "Cache eviction happens automatically on injury/assessment changes, " +
                      "so this endpoint is only needed for emergency manual resets."
    )
    @ApiResponse(responseCode = "204", description = "Cache evicted")
    public ResponseEntity<Void> evictCache(@PathVariable UUID athleteId) {
        injuryRiskService.evictRiskCache(athleteId);
        return ResponseEntity.noContent().build();
    }
}
