package com.feadev.eaids.event.controller;

import com.feadev.eaids.event.dto.request.GuestRegistrationRequest;
import com.feadev.eaids.event.dto.response.EventRegistrationResponse;
import com.feadev.eaids.event.model.enums.RegistrationStatus;
import com.feadev.eaids.event.service.EventRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Event Registration", description = "Attendance registration and management for events")
public class EventRegistrationController {

    private final EventRegistrationService eventRegistrationService;

    @PostMapping("/{eventId}/register")
    @Operation(summary = "Register for an event (authenticated)", description = "Registers the authenticated user for the specified event. Event must be in REGISTRATION_OPEN status.")
    @ApiResponse(responseCode = "201", description = "Successfully registered for the event")
    public ResponseEntity<EventRegistrationResponse> registerForEvent(
            @PathVariable UUID eventId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        EventRegistrationResponse response = eventRegistrationService.registerForEvent(eventId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{eventId}/register/guest")
    @Operation(summary = "Register for an event (guest)", description = "Registers a non-authenticated guest for the specified event. Requires name and email. Event must be in REGISTRATION_OPEN status.")
    @ApiResponse(responseCode = "201", description = "Guest successfully registered for the event")
    public ResponseEntity<EventRegistrationResponse> registerGuestForEvent(
            @PathVariable UUID eventId,
            @Valid @RequestBody GuestRegistrationRequest request) {
        EventRegistrationResponse response = eventRegistrationService.registerGuestForEvent(eventId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{eventId}/registrations/{registrationId}/cancel")
    @Operation(summary = "Cancel registration (authenticated)", description = "Cancels the authenticated user's registration for the event. Only REGISTERED or CONFIRMED registrations can be cancelled.")
    public ResponseEntity<EventRegistrationResponse> cancelRegistration(
            @PathVariable UUID eventId,
            @PathVariable UUID registrationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        EventRegistrationResponse response = eventRegistrationService.cancelRegistration(eventId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{eventId}/register/guest/cancel")
    @Operation(summary = "Cancel guest registration", description = "Cancels a guest registration by email. Only REGISTERED or CONFIRMED registrations can be cancelled.")
    public ResponseEntity<EventRegistrationResponse> cancelGuestRegistration(
            @PathVariable UUID eventId,
            @RequestBody Map<String, String> body) {
        String guestEmail = body.get("email");
        EventRegistrationResponse response = eventRegistrationService.cancelGuestRegistration(eventId, guestEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{eventId}/registrations")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "List event registrations", description = "Lists all registrations (authenticated and guest) for a specific event. Admin and Coach only.")
    public ResponseEntity<List<EventRegistrationResponse>> getEventRegistrations(
            @PathVariable UUID eventId) {
        List<EventRegistrationResponse> registrations = eventRegistrationService.getEventRegistrations(eventId);
        return ResponseEntity.ok(registrations);
    }

    @PatchMapping("/{eventId}/registrations/{registrationId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update registration status", description = "Updates the attendance status of a registration (CONFIRMED, ATTENDED, NO_SHOW). Admin only.")
    public ResponseEntity<EventRegistrationResponse> updateRegistrationStatus(
            @PathVariable UUID eventId,
            @PathVariable UUID registrationId,
            @RequestBody Map<String, String> body) {
        RegistrationStatus newStatus = RegistrationStatus.valueOf(body.get("status"));
        EventRegistrationResponse response = eventRegistrationService.updateRegistrationStatus(eventId, registrationId, newStatus);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-registrations")
    @Operation(summary = "Get my registrations", description = "Lists all event registrations for the authenticated user.")
    public ResponseEntity<List<EventRegistrationResponse>> getMyRegistrations(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        List<EventRegistrationResponse> registrations = eventRegistrationService.getMyRegistrations(userId);
        return ResponseEntity.ok(registrations);
    }
}
