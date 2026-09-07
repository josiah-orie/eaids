package com.feadev.eaids.academy.controller;

import com.feadev.eaids.academy.dto.request.CreateIndividualApplicationRequest;
import com.feadev.eaids.academy.dto.response.IndividualApplicationResponse;
import com.feadev.eaids.academy.service.AcademyIndividualApplicationService;
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
@RequestMapping("/api/v1/academy/applications/individuals")
@RequiredArgsConstructor
@Tag(name = "Academy - Individual Onboarding",
        description = "Public onboarding for individual academy applicants")
public class AcademyIndividualApplicationController {

    private final AcademyIndividualApplicationService service;

    @PostMapping
    @Operation(summary = "Submit an individual application",
            description = "Public endpoint. Creates a PENDING individual application. No authentication required.")
    @ApiResponse(responseCode = "201", description = "Application submitted successfully")
    public ResponseEntity<IndividualApplicationResponse> submit(
            @Valid @RequestBody CreateIndividualApplicationRequest request) {
        IndividualApplicationResponse response = service.submit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
