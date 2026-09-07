package com.feadev.eaids.registrations.controller;

import com.feadev.eaids.registrations.dto.response.CoachProfileResponse;
import com.feadev.eaids.registrations.service.CoachService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/coaches")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Coaches", description = "Administrative endpoints for retrieving coach profiles")
public class AdminCoachController {

    private final CoachService coachService;

    @GetMapping("/{coachUserId}")
    @Operation(summary = "Get coach profile by user UUID",
            description = "Retrieve a coach's complete profile, including documents, using the coach's User UUID")
    public ResponseEntity<CoachProfileResponse> getCoachProfile(@PathVariable UUID coachUserId) {
        return ResponseEntity.ok(coachService.getProfile(coachUserId));
    }
}
