package com.feadev.eaids.analytics.injuryrisk.controller;

import com.feadev.eaids.analytics.injuryrisk.dto.AthleteRiskSummaryResponse;
import com.feadev.eaids.analytics.injuryrisk.dto.HolisticRiskResponse;
import com.feadev.eaids.analytics.injuryrisk.dto.InjuryRiskResponse;
import com.feadev.eaids.analytics.injuryrisk.service.InjuryRiskService;
import com.feadev.eaids.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/analytics/injury-risk")
@RequiredArgsConstructor
public class InjuryRiskController {

    private final InjuryRiskService injuryRiskService;

    @GetMapping("/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<InjuryRiskResponse> getInjuryRisk(@PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(injuryRiskService.assessEnrollment(enrollmentId));
    }

    @PostMapping("/assess/{campId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<List<InjuryRiskResponse>> batchAssessCamp(@PathVariable UUID campId) {
        return ResponseEntity.ok(injuryRiskService.batchAssessCamp(campId));
    }

    /**
     * Returns the full holistic risk profile for an athlete.
     *
     * <ul>
     *   <li>{@code ADMIN} / {@code COACH} — receive the complete {@link HolisticRiskResponse}
     *       including all training factor scores and the medical dimension breakdown.</li>
     *   <li>Athlete accessing their own profile — receive the athlete-safe
     *       {@link AthleteRiskSummaryResponse} which omits raw factor scores and heuristic
     *       point totals.</li>
     * </ul>
     *
     * <p>The literal path segment {@code athlete} ensures Spring MVC does not
     * confuse this route with {@code /{enrollmentId}}.
     */
    @GetMapping("/athlete/{athleteUserId}/full")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH') or #athleteUserId == authentication.principal.id")
    public ResponseEntity<?> getAthleteFullRiskProfile(
            @PathVariable UUID athleteUserId,
            Authentication authentication) {

        HolisticRiskResponse full = injuryRiskService.getAthleteFullRiskProfile(athleteUserId);

        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        boolean isPrivileged = actor.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                            || a.getAuthority().equals("ROLE_COACH"));

        if (isPrivileged) {
            return ResponseEntity.ok(full);
        }

        // Athlete self-access: return the safe subset only
        AthleteRiskSummaryResponse summary = AthleteRiskSummaryResponse.builder()
                .athleteUserId(full.getAthleteUserId())
                .overallRiskLevel(full.getOverallRiskLevel())
                .medicalRiskLevel(full.getMedicalRiskLevel())
                .medicalFlags(full.getMedicalFlags())
                .medicalSummary(full.getMedicalSummary())
                .recommendations(full.getRecommendations())
                .assessedAt(full.getAssessedAt())
                .build();

        return ResponseEntity.ok(summary);
    }
}
