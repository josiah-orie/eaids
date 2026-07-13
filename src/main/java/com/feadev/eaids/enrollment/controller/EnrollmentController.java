package com.feadev.eaids.enrollment.controller;

import com.feadev.eaids.enrollment.dto.response.EnrollmentResponse;
import com.feadev.eaids.enrollment.model.enums.EnrollmentStatus;
import com.feadev.eaids.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/camps/{campId}")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/enroll")
    @PreAuthorize("hasRole('ATHLETE')")
    public ResponseEntity<EnrollmentResponse> enrollAthlete(
            @PathVariable UUID campId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        EnrollmentResponse response = enrollmentService.enrollAthlete(campId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/athletes")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<List<EnrollmentResponse>> getCampEnrollments(
            @PathVariable UUID campId,
            @RequestParam(required = false) EnrollmentStatus status) {
        List<EnrollmentResponse> enrollments;
        if (status != null) {
            enrollments = enrollmentService.getCampEnrollmentsByStatus(campId, status);
        } else {
            enrollments = enrollmentService.getCampEnrollments(campId);
        }
        return ResponseEntity.ok(enrollments);
    }

    @PatchMapping("/enrollments/{enrollmentId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentResponse> updateEnrollmentStatus(
            @PathVariable UUID campId,
            @PathVariable UUID enrollmentId,
            @RequestBody Map<String, String> body) {
        EnrollmentStatus newStatus = EnrollmentStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(enrollmentService.updateEnrollmentStatus(campId, enrollmentId, newStatus));
    }
}
