package com.feadev.eaids.camp.controller;

import com.feadev.eaids.camp.dto.request.CreateCampRequest;
import com.feadev.eaids.camp.dto.request.UpdateCampRequest;
import com.feadev.eaids.camp.dto.response.CampResponse;
import com.feadev.eaids.camp.model.enums.CampStatus;
import com.feadev.eaids.camp.model.enums.CampType;
import com.feadev.eaids.camp.service.CampService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/camps")
@RequiredArgsConstructor
@Tag(name = "Camp Management", description = "CRUD and lifecycle management for assessment camps")
public class CampController {

    private final CampService campService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new camp", description = "Creates a new assessment camp in DRAFT status")
    @ApiResponse(responseCode = "201", description = "Camp created successfully")
    public ResponseEntity<CampResponse> createCamp(@Valid @RequestBody CreateCampRequest request) {
        CampResponse response = campService.createCamp(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{campId}")
    public ResponseEntity<CampResponse> getCamp(@PathVariable UUID campId) {
        return ResponseEntity.ok(campService.getCamp(campId));
    }

    @GetMapping
    public ResponseEntity<List<CampResponse>> getAllCamps(
            @RequestParam(required = false) CampStatus status,
            @RequestParam(required = false) CampType type) {
        List<CampResponse> camps;
        if (status != null) {
            camps = campService.getCampsByStatus(status);
        } else if (type != null) {
            camps = campService.getCampsByType(type);
        } else {
            camps = campService.getAllCamps();
        }
        return ResponseEntity.ok(camps);
    }

    @PutMapping("/{campId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampResponse> updateCamp(
            @PathVariable UUID campId,
            @Valid @RequestBody UpdateCampRequest request) {
        return ResponseEntity.ok(campService.updateCamp(campId, request));
    }

    @DeleteMapping("/{campId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCamp(@PathVariable UUID campId) {
        campService.deleteCamp(campId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{campId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CampResponse> transitionStatus(
            @PathVariable UUID campId,
            @RequestBody Map<String, String> body) {
        CampStatus newStatus = CampStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(campService.transitionStatus(campId, newStatus));
    }
}
