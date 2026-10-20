package com.feadev.eaids.league.controller;

import com.feadev.eaids.league.dto.request.CreateLeagueRequest;
import com.feadev.eaids.league.dto.request.UpdateLeagueRequest;
import com.feadev.eaids.league.dto.response.LeagueResponse;
import com.feadev.eaids.league.dto.response.LeagueStandingsResponse;
import com.feadev.eaids.league.model.enums.LeagueStatus;
import com.feadev.eaids.league.service.LeagueService;
import com.feadev.eaids.league.service.LeagueStandingsService;
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
@RequestMapping("/api/v1/admin/leagues")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "League Management", description = "Full CRUD and lifecycle management for leagues")
public class LeagueController {

    private final LeagueService leagueService;
    private final LeagueStandingsService standingsService;

    @PostMapping
    @Operation(summary = "Create a new league", description = "Creates a new league in DRAFT status")
    @ApiResponse(responseCode = "201", description = "League created successfully")
    public ResponseEntity<LeagueResponse> createLeague(@Valid @RequestBody CreateLeagueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leagueService.createLeague(request));
    }

    @GetMapping("/{leagueId}")
    @Operation(summary = "Get a league by ID")
    public ResponseEntity<LeagueResponse> getLeague(@PathVariable UUID leagueId) {
        return ResponseEntity.ok(leagueService.getLeague(leagueId));
    }

    @GetMapping
    @Operation(summary = "List all leagues with optional filters", description = "Filter by status or country using query params")
    public ResponseEntity<List<LeagueResponse>> getAllLeagues(
            @RequestParam(required = false) LeagueStatus status,
            @RequestParam(required = false) String country) {
        if (status != null) return ResponseEntity.ok(leagueService.getLeaguesByStatus(status));
        if (country != null) return ResponseEntity.ok(leagueService.getLeaguesByCountry(country));
        return ResponseEntity.ok(leagueService.getAllLeagues());
    }

    @PutMapping("/{leagueId}")
    @Operation(summary = "Update a league", description = "Only DRAFT leagues can be updated")
    public ResponseEntity<LeagueResponse> updateLeague(
            @PathVariable UUID leagueId,
            @Valid @RequestBody UpdateLeagueRequest request) {
        return ResponseEntity.ok(leagueService.updateLeague(leagueId, request));
    }

    @DeleteMapping("/{leagueId}")
    @Operation(summary = "Delete a league", description = "Only DRAFT leagues can be deleted")
    public ResponseEntity<Void> deleteLeague(@PathVariable UUID leagueId) {
        leagueService.deleteLeague(leagueId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{leagueId}/status")
    @Operation(summary = "Transition league status", description = "DRAFT→ACTIVE→COMPLETED→ARCHIVED")
    public ResponseEntity<LeagueResponse> transitionStatus(
            @PathVariable UUID leagueId,
            @RequestBody Map<String, String> body) {
        LeagueStatus newStatus = LeagueStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(leagueService.transitionStatus(leagueId, newStatus));
    }

    @GetMapping("/{leagueId}/standings")
    @Operation(summary = "Get league standings",
               description = "Returns the standing table for every division in the league, " +
                             "ordered by tier rank. Optionally scope to a single division with " +
                             "?divisionId=. Teams are grouped by their assigned division; " +
                             "results are counted only from COMPLETED fixtures tagged to that division.")
    public ResponseEntity<LeagueStandingsResponse> getStandings(
            @PathVariable UUID leagueId,
            @RequestParam(required = false) UUID divisionId) {
        if (divisionId != null) {
            return ResponseEntity.ok(standingsService.getStandingsByDivision(leagueId, divisionId));
        }
        return ResponseEntity.ok(standingsService.getStandings(leagueId));
    }
}
