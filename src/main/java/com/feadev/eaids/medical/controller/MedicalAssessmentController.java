package com.feadev.eaids.medical.controller;

import com.feadev.eaids.medical.dto.request.CreateAssessmentRequest;
import com.feadev.eaids.medical.dto.request.UpdateAssessmentRequest;
import com.feadev.eaids.medical.dto.response.AssessmentSummaryResponse;
import com.feadev.eaids.medical.dto.response.MedicalAssessmentResponse;
import com.feadev.eaids.medical.model.enums.AssessmentStatus;
import com.feadev.eaids.medical.service.MedicalAssessmentService;
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
@Tag(name = "Medical Assessments",
     description = "Pre-participation and periodic medical assessments for athletes")
public class MedicalAssessmentController {

    private final MedicalAssessmentService assessmentService;

    @PostMapping("/api/v1/athletes/{athleteId}/medical-assessments")
    @PreAuthorize("hasAnyRole('MEDICAL_OFFICER','ADMIN')")
    @Operation(summary = "Create a medical assessment for an athlete")
    public ResponseEntity<MedicalAssessmentResponse> createAssessment(
            @PathVariable UUID athleteId,
            @Valid @RequestBody CreateAssessmentRequest request,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assessmentService.createAssessment(athleteId, request,
                        actor.getId(), actor.getRole()));
    }

    @GetMapping("/api/v1/athletes/{athleteId}/medical-assessments")
    @Operation(summary = "List assessments for an athlete",
               description = "Athletes receive summaries of their own assessments. " +
                             "Medical officers, physiotherapists, and admins receive full details.")
    public ResponseEntity<Page<?>> getAssessmentsForAthlete(
            @PathVariable UUID athleteId,
            Pageable pageable,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return switch (actor.getRole()) {
            case ROLE_MEDICAL_OFFICER, ROLE_ADMIN, ROLE_PHYSIOTHERAPIST ->
                    ResponseEntity.ok(assessmentService.getFullAssessmentsForAthlete(
                            athleteId, actor.getId(), actor.getRole(), pageable));
            default ->
                    ResponseEntity.ok(assessmentService.getAssessmentSummariesForAthlete(
                            athleteId, actor.getId(), actor.getRole(), pageable));
        };
    }

    @GetMapping("/api/v1/medical-assessments/{assessmentId}")
    @Operation(summary = "Get a medical assessment by ID")
    public ResponseEntity<Object> getAssessment(
            @PathVariable UUID assessmentId,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                assessmentService.getAssessment(assessmentId, actor.getId(), actor.getRole()));
    }

    @PutMapping("/api/v1/medical-assessments/{assessmentId}")
    @PreAuthorize("hasAnyRole('MEDICAL_OFFICER','ADMIN')")
    @Operation(summary = "Update a medical assessment — only DRAFT or SUBMITTED assessments")
    public ResponseEntity<MedicalAssessmentResponse> updateAssessment(
            @PathVariable UUID assessmentId,
            @RequestBody UpdateAssessmentRequest request,
            Authentication authentication) {
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                assessmentService.updateAssessment(assessmentId, request,
                        actor.getId(), actor.getRole()));
    }

    @PostMapping("/api/v1/medical-assessments/{assessmentId}/status")
    @PreAuthorize("hasAnyRole('MEDICAL_OFFICER','ADMIN')")
    @Operation(summary = "Transition assessment status",
               description = "DRAFT → SUBMITTED → UNDER_REVIEW → FINALISED. FINALISED → AMENDED.")
    public ResponseEntity<MedicalAssessmentResponse> transitionStatus(
            @PathVariable UUID assessmentId,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        AssessmentStatus newStatus = AssessmentStatus.valueOf(body.get("status").toUpperCase());
        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(
                assessmentService.transitionStatus(assessmentId, newStatus,
                        actor.getId(), actor.getRole()));
    }
}
