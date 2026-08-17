package com.feadev.eaids.event.controller;

import com.feadev.eaids.event.dto.request.CreateEventRequest;
import com.feadev.eaids.event.dto.request.UpdateEventRequest;
import com.feadev.eaids.event.dto.response.EventResponse;
import com.feadev.eaids.event.model.enums.EventStatus;
import com.feadev.eaids.event.service.EventService;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Tag(name = "Event Management", description = "CRUD and lifecycle management for events (exhibition games, showcases, etc.)")
public class EventController {

    private final EventService eventService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new event", description = "Creates a new event in DRAFT status. Admin only.")
    @ApiResponse(responseCode = "201", description = "Event created successfully")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestPart("event") CreateEventRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        EventResponse response = eventService.createEvent(request, image);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "Get event by ID", description = "Retrieves a single event by its ID. Available to all authenticated users.")
    public ResponseEntity<EventResponse> getEvent(@PathVariable UUID eventId) {
        return ResponseEntity.ok(eventService.getEvent(eventId));
    }

    @GetMapping
    @Operation(summary = "List all events", description = "Lists all events with pagination, sorting, and optional filtering. Open to all users (no authentication required).")
    public ResponseEntity<Page<EventResponse>> getAllEvents(
            @Parameter(description = "Filter by event status") @RequestParam(required = false) EventStatus status,
            @Parameter(description = "Filter by event type") @RequestParam(required = false) String eventType,
            @Parameter(description = "Filter by country") @RequestParam(required = false) String country,
            @Parameter(description = "Search by name, location, or description") @RequestParam(required = false) String search,
            @Parameter(description = "Filter events starting from this date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
            @Parameter(description = "Filter events starting up to this date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
            @Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default 20, max 100)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field (e.g. startDate, name, createdDate)") @RequestParam(defaultValue = "startDate") String sortBy,
            @Parameter(description = "Sort direction (asc or desc)") @RequestParam(defaultValue = "asc") String sortDir) {

        size = Math.min(size, 100);
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EventResponse> events = eventService.getEvents(status, eventType, country,
                search, startDateFrom, startDateTo, pageable);
        return ResponseEntity.ok(events);
    }

    @PutMapping(value = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an event", description = "Updates an event. Only DRAFT events can be updated. Admin only.")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable UUID eventId,
            @Valid @RequestPart("event") UpdateEventRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(eventService.updateEvent(eventId, request, image));
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an event", description = "Deletes an event. Only DRAFT events can be deleted. Admin only.")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{eventId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Transition event status", description = "Transitions the event to a new status following the lifecycle rules. Admin only.")
    public ResponseEntity<EventResponse> transitionStatus(
            @PathVariable UUID eventId,
            @RequestBody Map<String, String> body) {
        EventStatus newStatus = EventStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(eventService.transitionStatus(eventId, newStatus));
    }
}
