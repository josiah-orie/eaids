package com.feadev.eaids.scouting.controller;

import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.scouting.dto.request.CreateInterviewRequest;
import com.feadev.eaids.scouting.dto.request.ReviewInterviewRequest;
import com.feadev.eaids.scouting.dto.response.InterviewRequestResponse;
import com.feadev.eaids.scouting.model.enums.InterviewStatus;
import com.feadev.eaids.scouting.service.InterviewRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class InterviewRequestController {

    private final InterviewRequestService interviewRequestService;
    private final UserRepository userRepository;

    // Scout submits interview request
    @PostMapping("/api/v1/scouting/interview-requests")
    @PreAuthorize("hasRole('SCOUT')")
    public ResponseEntity<InterviewRequestResponse> submitRequest(
            @Valid @RequestBody CreateInterviewRequest request) {
        UUID scoutUserId = getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interviewRequestService.submitRequest(scoutUserId, request));
    }

    // Scout views their requests
    @GetMapping("/api/v1/scouting/interview-requests")
    @PreAuthorize("hasRole('SCOUT')")
    public ResponseEntity<Page<InterviewRequestResponse>> getMyRequests(Pageable pageable) {
        UUID scoutUserId = getCurrentUserId();
        return ResponseEntity.ok(interviewRequestService.getScoutRequests(scoutUserId, pageable));
    }

    // Admin views pending requests
    @GetMapping("/api/v1/admin/interview-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<InterviewRequestResponse>> getRequestsByStatus(
            @RequestParam(defaultValue = "PENDING") InterviewStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(interviewRequestService.getRequestsByStatus(status, pageable));
    }

    // Admin reviews (approves/rejects) a request
    @PatchMapping("/api/v1/admin/interview-requests/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InterviewRequestResponse> reviewRequest(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewInterviewRequest review) {
        return ResponseEntity.ok(interviewRequestService.reviewRequest(id, review));
    }

    private UUID getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        return user.getId();
    }
}
