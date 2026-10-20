package com.feadev.eaids.league.controller;

import com.feadev.eaids.league.dto.request.DraftPlayerRequest;
import com.feadev.eaids.league.dto.request.TransferPlayerRequest;
import com.feadev.eaids.league.dto.response.PlayerTransferResponse;
import com.feadev.eaids.league.dto.response.TeamPlayerResponse;
import com.feadev.eaids.league.service.PlayerDraftService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Player Drafting & Transfers", description = "Draft athletes to teams, manage transfers, loans, and releases")
public class PlayerDraftController {

    private final PlayerDraftService playerDraftService;

    // ── Roster endpoints (scoped to a team) ──────────────────────────────────

    @PostMapping("/api/v1/admin/teams/{teamId}/players")
    @Operation(summary = "Draft a camp-graduated athlete to a team",
               description = "Athlete must have ROLE_ATHLETE and at least one CONFIRMED camp enrollment.")
    public ResponseEntity<TeamPlayerResponse> draftPlayer(
            @PathVariable UUID teamId,
            @Valid @RequestBody DraftPlayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(playerDraftService.draftPlayer(teamId, request));
    }

    @GetMapping("/api/v1/admin/teams/{teamId}/players")
    @Operation(summary = "Get the active roster for a team")
    public ResponseEntity<List<TeamPlayerResponse>> getTeamRoster(@PathVariable UUID teamId) {
        return ResponseEntity.ok(playerDraftService.getTeamRoster(teamId));
    }

    @PostMapping("/api/v1/admin/teams/{teamId}/players/{teamPlayerId}/transfer")
    @Operation(summary = "Transfer or loan a player to another team",
               description = "Transfer type must be TRANSFER or LOAN.")
    public ResponseEntity<TeamPlayerResponse> transferPlayer(
            @PathVariable UUID teamId,
            @PathVariable UUID teamPlayerId,
            @Valid @RequestBody TransferPlayerRequest request) {
        return ResponseEntity.ok(playerDraftService.transferPlayer(teamPlayerId, request));
    }

    @DeleteMapping("/api/v1/admin/teams/{teamId}/players/{teamPlayerId}")
    @Operation(summary = "Release a player from a team",
               description = "Sets player status to TRANSFERRED and records a RELEASE transfer event.")
    public ResponseEntity<TeamPlayerResponse> releasePlayer(
            @PathVariable UUID teamId,
            @PathVariable UUID teamPlayerId,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(playerDraftService.releasePlayer(teamPlayerId, notes));
    }

    // ── Transfer history (scoped to an athlete) ───────────────────────────────

    @GetMapping("/api/v1/admin/players/{athleteUserId}/transfers")
    @Operation(summary = "Get full transfer history for an athlete")
    public ResponseEntity<List<PlayerTransferResponse>> getTransferHistory(
            @PathVariable UUID athleteUserId) {
        return ResponseEntity.ok(playerDraftService.getPlayerTransferHistory(athleteUserId));
    }
}
