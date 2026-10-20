package com.feadev.eaids.league.controller;

import com.feadev.eaids.league.dto.request.AssignStaffRequest;
import com.feadev.eaids.league.dto.request.UpdateStaffRequest;
import com.feadev.eaids.league.dto.response.LeagueStaffResponse;
import com.feadev.eaids.league.service.LeagueStaffService;
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
@RequestMapping("/api/v1/admin/leagues/{leagueId}/staff")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "League Staff Management", description = "Assign and manage league-scoped staff roles")
public class LeagueStaffController {

    private final LeagueStaffService staffService;

    @PostMapping
    @Operation(summary = "Assign a user a league-scoped staff role",
               description = "User must be ACTIVE. Same user can hold different roles on the same league but not duplicate the same role.")
    public ResponseEntity<LeagueStaffResponse> assignStaff(
            @PathVariable UUID leagueId,
            @Valid @RequestBody AssignStaffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(staffService.assignStaff(leagueId, request));
    }

    @GetMapping
    @Operation(summary = "List all staff assigned to a league")
    public ResponseEntity<List<LeagueStaffResponse>> getStaff(@PathVariable UUID leagueId) {
        return ResponseEntity.ok(staffService.getStaffForLeague(leagueId));
    }

    @PutMapping("/{staffId}")
    @Operation(summary = "Update a staff assignment role or notes")
    public ResponseEntity<LeagueStaffResponse> updateStaff(
            @PathVariable UUID leagueId,
            @PathVariable UUID staffId,
            @RequestBody UpdateStaffRequest request) {
        return ResponseEntity.ok(staffService.updateStaffAssignment(staffId, request));
    }

    @DeleteMapping("/{staffId}")
    @Operation(summary = "Remove a staff assignment from a league")
    public ResponseEntity<Void> removeStaff(
            @PathVariable UUID leagueId,
            @PathVariable UUID staffId) {
        staffService.removeStaff(staffId);
        return ResponseEntity.noContent().build();
    }
}
