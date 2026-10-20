package com.feadev.eaids.league.controller;

import com.feadev.eaids.league.dto.response.FixturePreGameStatsResponse;
import com.feadev.eaids.league.dto.response.MatchFixtureResponse;
import com.feadev.eaids.league.model.enums.FixtureStatus;
import com.feadev.eaids.league.service.LeaguePreGameStatsService;
import com.feadev.eaids.league.service.MatchFixtureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Read-only fixture endpoints accessible to any authenticated user
 * (scouts, coaches, athletes, admins).
 *
 * <p>Security is enforced at the path level in {@code SecurityConfig}:
 * {@code GET /api/v1/leagues/**} requires authentication.
 * No role restriction is applied here — all authenticated roles can browse
 * fixtures and view pre-game stats.
 */
@RestController
@RequestMapping("/api/v1/leagues/{leagueId}/fixtures")
@RequiredArgsConstructor
@Tag(name = "Fixture Info",
     description = "Read-only fixture list and pre-game stats for scouts, coaches, and staff")
public class PublicFixtureController {

    private final MatchFixtureService fixtureService;
    private final LeaguePreGameStatsService preGameStatsService;

    @GetMapping
    @Operation(summary = "List fixtures for a league",
               description = "Optionally filter by status or division. " +
                             "Accessible by any authenticated user.")
    public ResponseEntity<List<MatchFixtureResponse>> getFixtures(
            @PathVariable UUID leagueId,
            @RequestParam(required = false) FixtureStatus status,
            @RequestParam(required = false) UUID divisionId) {
        return ResponseEntity.ok(fixtureService.getFixturesByLeague(leagueId, status, divisionId));
    }

    @GetMapping("/{fixtureId}")
    @Operation(summary = "Get a specific fixture by ID",
               description = "Accessible by any authenticated user.")
    public ResponseEntity<MatchFixtureResponse> getFixture(
            @PathVariable UUID leagueId,
            @PathVariable UUID fixtureId) {
        return ResponseEntity.ok(fixtureService.getFixture(fixtureId));
    }

    @GetMapping("/{fixtureId}/pregame")
    @Operation(summary = "Get pre-game statistical breakdown for a fixture",
               description = "Returns team-level season averages (record, points/game, passing and " +
                             "rushing yards/game, opponent averages) and key player highlights " +
                             "(top QB, RB, WR, defender) derived from completed fixtures in the " +
                             "same league. Returns zeroed stats when no historical data exists. " +
                             "Accessible by any authenticated user.")
    public ResponseEntity<FixturePreGameStatsResponse> getPreGameStats(
            @PathVariable UUID leagueId,
            @PathVariable UUID fixtureId) {
        return ResponseEntity.ok(preGameStatsService.getPreGameStats(fixtureId));
    }
}
