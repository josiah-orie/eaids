package com.feadev.eaids.analytics.injuryrisk.controller;

import com.feadev.eaids.analytics.injuryrisk.dto.InjuryRiskResponse;
import com.feadev.eaids.analytics.injuryrisk.service.InjuryRiskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
}
