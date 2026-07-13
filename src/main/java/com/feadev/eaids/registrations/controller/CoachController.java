package com.feadev.eaids.registrations.controller;

import com.feadev.eaids.registrations.dto.request.CoachProfileUpdateRequest;
import com.feadev.eaids.registrations.dto.response.CoachProfileResponse;
import com.feadev.eaids.registrations.model.enums.DocumentType;
import com.feadev.eaids.security.CustomUserDetails;
import com.feadev.eaids.registrations.service.CoachService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/coaches")
@RequiredArgsConstructor
@Tag(name = "Coaches", description = "Endpoints for coach profile management and document uploads")
public class CoachController {

    private final CoachService coachService;

    @GetMapping("/me")
    @Operation(summary = "Get coach profile", description = "Retrieve the authenticated coach's complete profile including documents")
    public ResponseEntity<CoachProfileResponse> getProfile() {
        UUID userId = getCurrentUserId();
        CoachProfileResponse response = coachService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "Update coach profile", description = "Update the authenticated coach's profile fields. Only provided fields are updated.")
    public ResponseEntity<CoachProfileResponse> updateProfile(
            @Valid @RequestBody CoachProfileUpdateRequest request) {
        UUID userId = getCurrentUserId();
        CoachProfileResponse response = coachService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload document", description = "Upload a certification or credential document for verification. Allowed types: PDF, JPG, JPEG, PNG. Max size: 10MB.")
    public ResponseEntity<CoachProfileResponse.DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") DocumentType documentType) {
        UUID userId = getCurrentUserId();
        CoachProfileResponse.DocumentResponse response = coachService.uploadDocument(userId, file, documentType);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getId();
    }
}
