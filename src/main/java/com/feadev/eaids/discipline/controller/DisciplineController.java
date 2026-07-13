package com.feadev.eaids.discipline.controller;

import com.feadev.eaids.discipline.dto.request.RecordInfractionRequest;
import com.feadev.eaids.discipline.dto.response.DisciplineResponse;
import com.feadev.eaids.discipline.dto.response.DisciplineSummaryResponse;
import com.feadev.eaids.discipline.service.DisciplineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/camps/{campId}/discipline")
@RequiredArgsConstructor
public class DisciplineController {

    private final DisciplineService disciplineService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<DisciplineResponse> recordInfraction(
            @PathVariable UUID campId,
            @Valid @RequestBody RecordInfractionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(disciplineService.recordInfraction(campId, request, userDetails.getUsername()));
    }

    @GetMapping("/enrollment/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<List<DisciplineResponse>> getInfractionsByEnrollment(
            @PathVariable UUID campId,
            @PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(disciplineService.getInfractionsByEnrollment(enrollmentId));
    }

    @GetMapping("/enrollment/{enrollmentId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<DisciplineSummaryResponse> getDisciplineSummary(
            @PathVariable UUID campId,
            @PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(disciplineService.getDisciplineSummary(enrollmentId));
    }
}
