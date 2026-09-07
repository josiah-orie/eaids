package com.feadev.eaids.academy.controller;

import com.feadev.eaids.academy.dto.request.AssignStaffRequest;
import com.feadev.eaids.academy.dto.response.ScheduleStaffResponse;
import com.feadev.eaids.academy.service.AcademyScheduleStaffService;
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
@RequestMapping("/api/v1/academy/admin/schedules/{scheduleId}/staff")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Academy - Admin Schedule Staff",
        description = "Admin assignment of coaches and scouts to training schedules")
public class AcademyScheduleStaffController {

    private final AcademyScheduleStaffService service;

    @PostMapping
    @Operation(summary = "Assign a coach or scout to a training schedule",
            description = "Assigns an existing coach (CoachProfile id) or scout (ScoutProfile id) to the schedule. Admin only.")
    @ApiResponse(responseCode = "201", description = "Staff assigned successfully")
    public ResponseEntity<ScheduleStaffResponse> assign(
            @PathVariable UUID scheduleId,
            @Valid @RequestBody AssignStaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.assignStaff(scheduleId, request));
    }

    @GetMapping
    @Operation(summary = "List staff assigned to a training schedule")
    public ResponseEntity<List<ScheduleStaffResponse>> list(@PathVariable UUID scheduleId) {
        return ResponseEntity.ok(service.listStaff(scheduleId));
    }

    @DeleteMapping("/{staffAssignmentId}")
    @Operation(summary = "Remove a staff assignment from a training schedule")
    public ResponseEntity<Void> remove(
            @PathVariable UUID scheduleId,
            @PathVariable UUID staffAssignmentId) {
        service.removeStaff(scheduleId, staffAssignmentId);
        return ResponseEntity.noContent().build();
    }
}
