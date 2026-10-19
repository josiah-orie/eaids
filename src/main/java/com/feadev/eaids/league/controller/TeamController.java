package com.feadev.eaids.league.controller;

import com.feadev.eaids.league.dto.request.AssignTeamCoachRequest;
import com.feadev.eaids.league.dto.request.CreateTeamRequest;
import com.feadev.eaids.league.dto.request.UpdateTeamRequest;
import com.feadev.eaids.league.dto.response.TeamCoachResponse;
import com.feadev.eaids.league.dto.response.TeamResponse;
import com.feadev.eaids.league.model.enums.TeamCoachRole;
import com.feadev.eaids.league.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/teams")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Team Management", description = "Full CRUD, logo management, and coaching staff for teams")
public class TeamController {

    private final TeamService teamService;

    // ── Team CRUD ─────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a new team within a league")
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.createTeam(request));
    }

    @GetMapping("/{teamId}")
    @Operation(summary = "Get a team by ID — includes current head coach and assistant coach")
    public ResponseEntity<TeamResponse> getTeam(@PathVariable UUID teamId) {
        return ResponseEntity.ok(teamService.getTeam(teamId));
    }

    @GetMapping
    @Operation(summary = "List teams — filter by leagueId or divisionId, or list all")
    public ResponseEntity<List<TeamResponse>> getTeams(
            @RequestParam(required = false) UUID leagueId,
            @RequestParam(required = false) UUID divisionId) {
        if (leagueId != null) return ResponseEntity.ok(teamService.getTeamsByLeague(leagueId));
        if (divisionId != null) return ResponseEntity.ok(teamService.getTeamsByDivision(divisionId));
        return ResponseEntity.ok(teamService.getAllTeams());
    }

    @PutMapping("/{teamId}")
    @Operation(summary = "Update a team")
    public ResponseEntity<TeamResponse> updateTeam(
            @PathVariable UUID teamId,
            @Valid @RequestBody UpdateTeamRequest request) {
        return ResponseEntity.ok(teamService.updateTeam(teamId, request));
    }

    @DeleteMapping("/{teamId}")
    @Operation(summary = "Delete a team — blocked if active players exist")
    public ResponseEntity<Void> deleteTeam(@PathVariable UUID teamId) {
        teamService.deleteTeam(teamId);
        return ResponseEntity.noContent().build();
    }

    // ── Logo ──────────────────────────────────────────────────────────────────

    @PostMapping(value = "/{teamId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload or replace team logo (JPG/PNG, max 5MB)")
    public ResponseEntity<TeamResponse> uploadLogo(
            @PathVariable UUID teamId,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(teamService.uploadTeamLogo(teamId, file));
    }

    @DeleteMapping("/{teamId}/logo")
    @Operation(summary = "Delete team logo")
    public ResponseEntity<TeamResponse> deleteLogo(@PathVariable UUID teamId) {
        return ResponseEntity.ok(teamService.deleteTeamLogo(teamId));
    }

    // ── Roster ────────────────────────────────────────────────────────────────

    // ── Coaching staff ────────────────────────────────────────────────────────

    @PostMapping("/{teamId}/coaches")
    @Operation(
        summary = "Assign a head coach or assistant coach to a team",
        description = "Each team can have one HEAD_COACH and one ASSISTANT_COACH. " +
                      "Returns 409 if that role is already filled — use PUT to replace.")
    public ResponseEntity<TeamCoachResponse> assignCoach(
            @PathVariable UUID teamId,
            @Valid @RequestBody AssignTeamCoachRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamService.assignCoach(teamId, request));
    }

    @PutMapping("/{teamId}/coaches/{role}")
    @Operation(
        summary = "Replace the coach in a given role",
        description = "Updates the HEAD_COACH or ASSISTANT_COACH assignment to a different user. " +
                      "The role path variable must be HEAD_COACH or ASSISTANT_COACH.")
    public ResponseEntity<TeamCoachResponse> updateCoach(
            @PathVariable UUID teamId,
            @PathVariable TeamCoachRole role,
            @Valid @RequestBody AssignTeamCoachRequest request) {
        return ResponseEntity.ok(teamService.updateCoach(teamId, role, request));
    }

    @GetMapping("/{teamId}/coaches")
    @Operation(summary = "List all coaching staff currently assigned to a team")
    public ResponseEntity<List<TeamCoachResponse>> getCoaches(@PathVariable UUID teamId) {
        return ResponseEntity.ok(teamService.getCoaches(teamId));
    }

    @DeleteMapping("/{teamId}/coaches/{role}")
    @Operation(
        summary = "Remove the coach in a given role from a team",
        description = "The role path variable must be HEAD_COACH or ASSISTANT_COACH.")
    public ResponseEntity<Void> removeCoach(
            @PathVariable UUID teamId,
            @PathVariable TeamCoachRole role) {
        teamService.removeCoach(teamId, role);
        return ResponseEntity.noContent().build();
    }
}
