package com.feadev.eaids.league.controller;

import com.feadev.eaids.league.dto.request.CreateDivisionRequest;
import com.feadev.eaids.league.dto.request.UpdateDivisionRequest;
import com.feadev.eaids.league.dto.response.DivisionResponse;
import com.feadev.eaids.league.service.LeagueDivisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/leagues/{leagueId}/divisions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "League Division Management", description = "Manage ordered divisions within a league")
public class LeagueDivisionController {

    private final LeagueDivisionService divisionService;

    @PostMapping
    @Operation(summary = "Create a division within a league")
    public ResponseEntity<DivisionResponse> createDivision(
            @PathVariable UUID leagueId,
            @Valid @RequestBody CreateDivisionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(divisionService.createDivision(leagueId, request));
    }

    @GetMapping
    @Operation(summary = "List all divisions for a league ordered by tier rank")
    public ResponseEntity<List<DivisionResponse>> getDivisions(@PathVariable UUID leagueId) {
        return ResponseEntity.ok(divisionService.getDivisionsForLeague(leagueId));
    }

    @GetMapping("/{divisionId}")
    @Operation(summary = "Get a specific division by ID")
    public ResponseEntity<DivisionResponse> getDivision(
            @PathVariable UUID leagueId,
            @PathVariable UUID divisionId) {
        return ResponseEntity.ok(divisionService.getDivision(divisionId));
    }

    @PutMapping("/{divisionId}")
    @Operation(summary = "Update a division name, tier rank, or description")
    public ResponseEntity<DivisionResponse> updateDivision(
            @PathVariable UUID leagueId,
            @PathVariable UUID divisionId,
            @Valid @RequestBody UpdateDivisionRequest request) {
        return ResponseEntity.ok(divisionService.updateDivision(divisionId, request));
    }

    @DeleteMapping("/{divisionId}")
    @Operation(summary = "Delete a division — blocked if teams are assigned")
    public ResponseEntity<Void> deleteDivision(
            @PathVariable UUID leagueId,
            @PathVariable UUID divisionId) {
        divisionService.deleteDivision(divisionId);
        return ResponseEntity.noContent().build();
    }
}
