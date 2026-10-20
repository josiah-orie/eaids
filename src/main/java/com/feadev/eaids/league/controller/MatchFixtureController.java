package com.feadev.eaids.league.controller;

import com.feadev.eaids.league.dto.request.CreateFixtureRequest;
import com.feadev.eaids.league.dto.request.RecordLeagueStatsRequest;
import com.feadev.eaids.league.dto.request.TransitionFixtureRequest;
import com.feadev.eaids.league.dto.request.UpdateFixtureRequest;
import com.feadev.eaids.league.dto.response.FixturePreGameStatsResponse;
import com.feadev.eaids.league.dto.response.LeagueStatEntryResponse;
import com.feadev.eaids.league.dto.response.MatchFixtureResponse;
import com.feadev.eaids.league.model.enums.FixtureStatus;
import com.feadev.eaids.league.service.LeaguePreGameStatsService;
import com.feadev.eaids.league.service.MatchFixtureService;
import com.feadev.eaids.matchstats.model.MatchStatEntry;
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
@RequestMapping("/api/v1/admin/leagues/{leagueId}/fixtures")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Match Fixture Management", description = "Schedule and manage match fixtures within a league")
public class MatchFixtureController {

    private final MatchFixtureService fixtureService;
    private final LeaguePreGameStatsService leaguePreGameStatsService;

    @PostMapping
    @Operation(summary = "Create a fixture between two teams in this league")
    public ResponseEntity<MatchFixtureResponse> createFixture(
            @PathVariable UUID leagueId,
            @Valid @RequestBody CreateFixtureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(fixtureService.createFixture(leagueId, request));
    }

    @GetMapping
    @Operation(summary = "List fixtures for a league, optionally filtered by status or division")
    public ResponseEntity<List<MatchFixtureResponse>> getFixtures(
            @PathVariable UUID leagueId,
            @RequestParam(required = false) FixtureStatus status,
            @RequestParam(required = false) UUID divisionId) {
        return ResponseEntity.ok(fixtureService.getFixturesByLeague(leagueId, status, divisionId));
    }

    @GetMapping("/{fixtureId}")
    @Operation(summary = "Get a specific fixture by ID")
    public ResponseEntity<MatchFixtureResponse> getFixture(
            @PathVariable UUID leagueId,
            @PathVariable UUID fixtureId) {
        return ResponseEntity.ok(fixtureService.getFixture(fixtureId));
    }

    @PutMapping("/{fixtureId}")
    @Operation(summary = "Update fixture details — only allowed when SCHEDULED or POSTPONED")
    public ResponseEntity<MatchFixtureResponse> updateFixture(
            @PathVariable UUID leagueId,
            @PathVariable UUID fixtureId,
            @RequestBody UpdateFixtureRequest request) {
        return ResponseEntity.ok(fixtureService.updateFixture(fixtureId, request));
    }

    @PatchMapping("/{fixtureId}/status")
    @Operation(summary = "Transition fixture status",
               description = "SCHEDULED→IN_PROGRESS→COMPLETED. Scores required when completing. " +
                             "SCHEDULED/POSTPONED→CANCELLED.")
    public ResponseEntity<MatchFixtureResponse> transitionStatus(
            @PathVariable UUID leagueId,
            @PathVariable UUID fixtureId,
            @Valid @RequestBody TransitionFixtureRequest request) {
        return ResponseEntity.ok(fixtureService.transitionStatus(fixtureId, request));
    }

    @DeleteMapping("/{fixtureId}")
    @Operation(summary = "Delete a fixture — only SCHEDULED or POSTPONED fixtures can be deleted")
    public ResponseEntity<Void> deleteFixture(
            @PathVariable UUID leagueId,
            @PathVariable UUID fixtureId) {
        fixtureService.deleteFixture(fixtureId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{fixtureId}/stats")
    @Operation(summary = "Get player match stats linked to this fixture")
    public ResponseEntity<List<MatchStatEntry>> getFixtureStats(
            @PathVariable UUID leagueId,
            @PathVariable UUID fixtureId) {
        return ResponseEntity.ok(fixtureService.getFixtureStats(fixtureId));
    }

    @PostMapping("/{fixtureId}/league-stats")
    @Operation(summary = "Record player league stats for a fixture",
               description = "Fixture must be IN_PROGRESS or COMPLETED. " +
                             "Each teamPlayerId must belong to one of the fixture's two teams.")
    public ResponseEntity<List<LeagueStatEntryResponse>> recordLeagueStats(
            @PathVariable UUID leagueId,
            @PathVariable UUID fixtureId,
            @Valid @RequestBody RecordLeagueStatsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaguePreGameStatsService.recordLeagueStats(fixtureId, request));
    }

    @GetMapping("/{fixtureId}/pregame")
    @Operation(summary = "Get pre-game statistical breakdown for a fixture",
               description = "Returns team-level season averages and key player highlights " +
                             "derived from completed fixtures in the same league.")
    public ResponseEntity<FixturePreGameStatsResponse> getPreGameStats(
            @PathVariable UUID leagueId,
            @PathVariable UUID fixtureId) {
        return ResponseEntity.ok(leaguePreGameStatsService.getPreGameStats(fixtureId));
    }
}
