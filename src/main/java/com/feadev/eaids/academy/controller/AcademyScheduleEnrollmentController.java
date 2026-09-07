package com.feadev.eaids.academy.controller;

import com.feadev.eaids.academy.dto.request.EnrollApplicantRequest;
import com.feadev.eaids.academy.dto.response.EnrollmentResponse;
import com.feadev.eaids.academy.service.AcademyScheduleEnrollmentService;
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
@RequestMapping("/api/v1/academy/admin/schedules/{scheduleId}/enrollments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Academy - Admin Schedule Enrollments",
        description = "Admin enrollment of approved applicants into training schedules")
public class AcademyScheduleEnrollmentController {

    private final AcademyScheduleEnrollmentService service;

    @PostMapping
    @Operation(summary = "Enroll an approved applicant into a training schedule",
            description = "Enrolls an APPROVED individual or institution applicant. Enforces target-type match and capacity. Admin only.")
    @ApiResponse(responseCode = "201", description = "Applicant enrolled successfully")
    public ResponseEntity<EnrollmentResponse> enroll(
            @PathVariable UUID scheduleId,
            @Valid @RequestBody EnrollApplicantRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.enroll(scheduleId, request));
    }

    @GetMapping
    @Operation(summary = "List enrollments for a training schedule")
    public ResponseEntity<List<EnrollmentResponse>> list(@PathVariable UUID scheduleId) {
        return ResponseEntity.ok(service.listEnrollments(scheduleId));
    }

    @DeleteMapping("/{enrollmentId}")
    @Operation(summary = "Remove an enrollment from a training schedule")
    public ResponseEntity<Void> unenroll(
            @PathVariable UUID scheduleId,
            @PathVariable UUID enrollmentId) {
        service.unenroll(scheduleId, enrollmentId);
        return ResponseEntity.noContent().build();
    }
}
