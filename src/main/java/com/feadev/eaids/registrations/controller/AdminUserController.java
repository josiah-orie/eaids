package com.feadev.eaids.registrations.controller;

import com.feadev.eaids.registrations.dto.request.CreateUserRequest;
import com.feadev.eaids.registrations.dto.request.UpdateUserRequest;
import com.feadev.eaids.registrations.dto.response.MessageResponse;
import com.feadev.eaids.registrations.dto.response.UserDetailResponse;
import com.feadev.eaids.registrations.dto.response.UserListResponse;
import com.feadev.eaids.registrations.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin User Management", description = "CRUD endpoints for managing users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "Create a new user", description = "Creates a new user with the specified role and credentials")
    @PostMapping
    public ResponseEntity<UserDetailResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        UserDetailResponse response = adminUserService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get user by ID", description = "Returns detailed information about a specific user")
    @GetMapping("/{id}")
    public ResponseEntity<UserDetailResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable UUID id) {

        UserDetailResponse response = adminUserService.getUserById(id);
        return ResponseEntity.ok(response);
    }

//    @Operation(summary = "List all users", description = "Returns a paginated list of users with optional filtering")
//    @GetMapping
//    public ResponseEntity<Page<UserListResponse>> getAllUsersList(
//            @Parameter(description = "Filter by user role") @RequestParam(required = false) String role,
//            @Parameter(description = "Filter by user status") @RequestParam(required = false) String status,
//            @Parameter(description = "Search by email or phone number") @RequestParam(required = false) String search,
//            @Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") int page,
//            @Parameter(description = "Page size (default 20, max 100)") @RequestParam(defaultValue = "20") int size) {
//
//        size = Math.min(size, 100);
//        Pageable pageable = PageRequest.of(page, size);
//
//        Page<UserListResponse> users = adminUserService.getAllUsers(role, status, search, pageable);
//        return ResponseEntity.ok(users);
//    }

    @Operation(summary = "Update user", description = "Updates user fields. Only provided fields are updated (partial update)")
    @PutMapping("/{id}")
    public ResponseEntity<UserDetailResponse> updateUser(
            @Parameter(description = "User ID") @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {

        UserDetailResponse response = adminUserService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete user", description = "Permanently deletes a user from the system")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteUser(
            @Parameter(description = "User ID") @PathVariable UUID id) {

        MessageResponse response = adminUserService.deleteUser(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Enable user", description = "Enables a user account, allowing them to log in")
    @PatchMapping("/{id}/enable")
    public ResponseEntity<MessageResponse> enableUser(
            @Parameter(description = "User ID") @PathVariable UUID id) {

        MessageResponse response = adminUserService.toggleUserStatus(id, true);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Disable user", description = "Disables a user account, preventing them from logging in")
    @PatchMapping("/{id}/disable")
    public ResponseEntity<MessageResponse> disableUser(
            @Parameter(description = "User ID") @PathVariable UUID id) {

        MessageResponse response = adminUserService.toggleUserStatus(id, false);
        return ResponseEntity.ok(response);
    }
}
