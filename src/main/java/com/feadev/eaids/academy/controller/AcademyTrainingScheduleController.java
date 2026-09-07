package com.feadev.eaids.academy.controller;

import com.feadev.eaids.academy.dto.request.CreateTrainingScheduleRequest;
import com.feadev.eaids.academy.dto.request.UpdateTrainingScheduleRequest;
import com.feadev.eaids.academy.dto.response.TrainingScheduleResponse;
import com.feadev.eaids.academy.service.AcademyTrainingScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/academy/admin/schedules")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Academy - Admin Training Schedules",
        description = "Admin management of academy training schedules")
public class AcademyTrainingScheduleController {

    private final AcademyTrainingScheduleService service;

    @PostMapping
    @Operation(summary = "Create a training schedule",
            description = "Creates a training schedule in DRAFT status. Admin only.")
    @ApiResponse(responseCode = "201", description = "Schedule created successfully")
    public ResponseEntity<TrainingScheduleResponse> create(
            @Valid @RequestBody CreateTrainingScheduleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{scheduleId}")
    @Operation(summary = "Get a training schedule by ID")
    public ResponseEntity<TrainingScheduleResponse> get(@PathVariable UUID scheduleId) {
        return ResponseEntity.ok(service.get(scheduleId));
    }

    @GetMapping
    @Operation(summary = "List training schedules",
            description = "Lists schedules with pagination and optional filters. Admin only.")
    public ResponseEntity<Page<TrainingScheduleResponse>> list(
            @Parameter(description = "Filter by age category (UNDER_14, UNDER_16, UNDER_18, UNDER_21, OPEN)")
            @RequestParam(required = false) String ageCategory,
            @Parameter(description = "Filter by target type (INDIVIDUAL, INSTITUTION, BOTH)")
            @RequestParam(required = false) String targetType,
            @Parameter(description = "Filter by status (DRAFT, PUBLISHED, CANCELLED, COMPLETED)")
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        size = Math.min(size, 100);
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(service.list(ageCategory, targetType, status, pageable));
    }

    @PutMapping("/{scheduleId}")
    @Operation(summary = "Update a training schedule",
            description = "Updates a DRAFT schedule. Admin only.")
    public ResponseEntity<TrainingScheduleResponse> update(
            @PathVariable UUID scheduleId,
            @Valid @RequestBody UpdateTrainingScheduleRequest request) {
        return ResponseEntity.ok(service.update(scheduleId, request));
    }

    @PatchMapping("/{scheduleId}/publish")
    @Operation(summary = "Publish a training schedule",
            description = "Transitions a DRAFT schedule to PUBLISHED. Admin only.")
    public ResponseEntity<TrainingScheduleResponse> publish(@PathVariable UUID scheduleId) {
        return ResponseEntity.ok(service.publish(scheduleId));
    }

    @PatchMapping("/{scheduleId}/cancel")
    @Operation(summary = "Cancel a training schedule",
            description = "Transitions a schedule to CANCELLED. Admin only.")
    public ResponseEntity<TrainingScheduleResponse> cancel(@PathVariable UUID scheduleId) {
        return ResponseEntity.ok(service.cancel(scheduleId));
    }
}
