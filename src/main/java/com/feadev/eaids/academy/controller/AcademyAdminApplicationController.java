package com.feadev.eaids.academy.controller;

import com.feadev.eaids.academy.dto.request.ReviewApplicationRequest;
import com.feadev.eaids.academy.dto.response.IndividualApplicationResponse;
import com.feadev.eaids.academy.dto.response.InstitutionApplicationResponse;
import com.feadev.eaids.academy.service.AcademyAdminApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/academy/admin/applications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Academy - Admin Applications",
        description = "Admin review of academy applications (approve/reject with reason)")
public class AcademyAdminApplicationController {

    private final AcademyAdminApplicationService service;

    @GetMapping("/individuals")
    @Operation(summary = "List individual applications",
            description = "Lists individual applications with pagination and optional status filter. Admin only.")
    public ResponseEntity<Page<IndividualApplicationResponse>> listIndividuals(
            @Parameter(description = "Filter by status (PENDING, APPROVED, REJECTED)")
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(service.listIndividuals(status, buildPageable(page, size, sortBy, sortDir)));
    }

    @GetMapping("/institutions")
    @Operation(summary = "List institution applications",
            description = "Lists institution applications with pagination and optional status filter. Admin only.")
    public ResponseEntity<Page<InstitutionApplicationResponse>> listInstitutions(
            @Parameter(description = "Filter by status (PENDING, APPROVED, REJECTED)")
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(service.listInstitutions(status, buildPageable(page, size, sortBy, sortDir)));
    }

    @PostMapping("/individuals/{applicationId}/review")
    @Operation(summary = "Approve or reject an individual application",
            description = "Reviews a PENDING individual application. A reason is required to reject. Admin only.")
    public ResponseEntity<IndividualApplicationResponse> reviewIndividual(
            @PathVariable UUID applicationId,
            @Valid @RequestBody ReviewApplicationRequest request) {
        return ResponseEntity.ok(service.reviewIndividual(applicationId, request));
    }

    @PostMapping("/institutions/{applicationId}/review")
    @Operation(summary = "Approve or reject an institution application",
            description = "Reviews a PENDING institution application. A reason is required to reject. Admin only.")
    public ResponseEntity<InstitutionApplicationResponse> reviewInstitution(
            @PathVariable UUID applicationId,
            @Valid @RequestBody ReviewApplicationRequest request) {
        return ResponseEntity.ok(service.reviewInstitution(applicationId, request));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        size = Math.min(size, 100);
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        return PageRequest.of(page, size, sort);
    }
}
