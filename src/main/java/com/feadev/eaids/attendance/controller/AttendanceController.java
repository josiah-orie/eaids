package com.feadev.eaids.attendance.controller;

import com.feadev.eaids.attendance.dto.request.BatchAttendanceRequest;
import com.feadev.eaids.attendance.dto.response.AttendanceResponse;
import com.feadev.eaids.attendance.dto.response.AttendanceSummaryResponse;
import com.feadev.eaids.attendance.service.AttendanceService;
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
@RequestMapping("/api/v1/camps/{campId}/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<List<AttendanceResponse>> recordBatchAttendance(
            @PathVariable UUID campId,
            @Valid @RequestBody BatchAttendanceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AttendanceResponse> responses = attendanceService.recordBatchAttendance(
                campId, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @GetMapping("/enrollment/{enrollmentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceByEnrollment(
            @PathVariable UUID campId,
            @PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(attendanceService.getAttendanceByEnrollment(enrollmentId));
    }

    @GetMapping("/enrollment/{enrollmentId}/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH', 'SCOUT')")
    public ResponseEntity<AttendanceSummaryResponse> getAttendanceSummary(
            @PathVariable UUID campId,
            @PathVariable UUID enrollmentId) {
        return ResponseEntity.ok(attendanceService.getAttendanceSummary(enrollmentId));
    }
}
