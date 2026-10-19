package com.feadev.eaids.league.controller;

import com.feadev.eaids.league.dto.response.TeamPlayerResponse;
import com.feadev.eaids.league.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leagues/{leagueId}/teams/{teamId}")
@RequiredArgsConstructor
@Tag(name = "Team Roster", description = "Team roster — list players for a specific team in a league")
public class TeamRosterController {

    private final TeamService teamService;

    @GetMapping("/players")
    @PreAuthorize("hasAnyRole('ADMIN','COACH','SCOUT')")
    @Operation(summary = "List all players in a team — useful for looking up teamPlayerId values for post-game stats")
    public ResponseEntity<List<TeamPlayerResponse>> getTeamPlayers(
            @PathVariable UUID leagueId,
            @PathVariable UUID teamId) {
        return ResponseEntity.ok(teamService.getTeamPlayers(leagueId, teamId));
    }
}
