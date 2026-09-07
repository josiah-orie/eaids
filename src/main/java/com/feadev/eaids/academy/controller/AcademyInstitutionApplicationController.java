package com.feadev.eaids.academy.controller;

import com.feadev.eaids.academy.dto.request.CreateInstitutionApplicationRequest;
import com.feadev.eaids.academy.dto.response.InstitutionApplicationResponse;
import com.feadev.eaids.academy.service.AcademyInstitutionApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/academy/applications/institutions")
@RequiredArgsConstructor
@Tag(name = "Academy - Institution Onboarding",
        description = "Public onboarding for institution academy applicants")
public class AcademyInstitutionApplicationController {

    private final AcademyInstitutionApplicationService service;

    @PostMapping
    @Operation(summary = "Submit an institution application",
            description = "Public endpoint. Creates a PENDING institution application. No authentication required.")
    @ApiResponse(responseCode = "201", description = "Application submitted successfully")
    public ResponseEntity<InstitutionApplicationResponse> submit(
            @Valid @RequestBody CreateInstitutionApplicationRequest request) {
        InstitutionApplicationResponse response = service.submit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
