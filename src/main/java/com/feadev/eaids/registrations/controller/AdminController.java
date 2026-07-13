package com.feadev.eaids.registrations.controller;

import com.feadev.eaids.registrations.dto.response.AuditLogResponse;
import com.feadev.eaids.registrations.dto.response.MessageResponse;
import com.feadev.eaids.registrations.dto.response.UserListResponse;
import com.feadev.eaids.registrations.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "List all users", description = "Returns a paginated list of users with optional filtering by role, status, and search term")
    @GetMapping("/users")
    public ResponseEntity<Page<UserListResponse>> listUsers(
            @Parameter(description = "Filter by user role") @RequestParam(required = false) String role,
            @Parameter(description = "Filter by user status") @RequestParam(required = false) String status,
            @Parameter(description = "Search term for email, phone, or name") @RequestParam(required = false) String search,
            @Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 20, max 100)") @RequestParam(defaultValue = "20") int size) {

        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size);

        Page<UserListResponse> users = adminService.listUsers(role, status, search, pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Approve athlete registration", description = "Approves an athlete with UNDER_REVIEW status, setting them to ACTIVE")
    @PutMapping("/athletes/{id}/approve")
    public ResponseEntity<MessageResponse> approveAthlete(
            @Parameter(description = "Athlete user ID") @PathVariable UUID id) {

        MessageResponse response = adminService.approveAthlete(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verify coach credentials", description = "Verifies a coach with UNDER_REVIEW status, setting them to ACTIVE")
    @PutMapping("/coaches/{id}/verify")
    public ResponseEntity<MessageResponse> verifyCoach(
            @Parameter(description = "Coach user ID") @PathVariable UUID id) {

        MessageResponse response = adminService.verifyCoach(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get audit logs", description = "Returns a paginated list of audit log entries with optional filtering")
    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @Parameter(description = "Filter by action type") @RequestParam(required = false) String action,
            @Parameter(description = "Filter by user ID") @RequestParam(required = false) UUID userId,
            @Parameter(description = "Filter from date (ISO format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "Filter to date (ISO format)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 20, max 100)") @RequestParam(defaultValue = "20") int size) {

        size = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, size);

        Page<AuditLogResponse> auditLogs = adminService.getAuditLogs(action, userId, from, to, pageable);
        return ResponseEntity.ok(auditLogs);
    }
}
