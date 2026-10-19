package com.feadev.eaids.league.controller;

import com.feadev.eaids.league.dto.request.UpdatePostGameStatsRequest;
import com.feadev.eaids.league.dto.response.FixturePostGameResponse;
import com.feadev.eaids.league.dto.response.PostGameSummaryResponse;
import com.feadev.eaids.league.service.PostGameStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Post-Game Statistics", description = "Per-athlete post-game summaries computed from league stat entries")
public class PostGameStatsController {

    private final PostGameStatsService postGameStatsService;

    @GetMapping("/api/v1/fixtures/{fixtureId}/post-game")
    @Operation(
        summary = "Get post-game stats for a fixture",
        description = "Returns per-athlete statistics grouped into home and away teams. " +
                      "Only available once the fixture is COMPLETED and the async computation has run."
    )
    @ApiResponse(responseCode = "200", description = "Post-game stats returned")
    @ApiResponse(responseCode = "404", description = "Fixture not found")
    public ResponseEntity<FixturePostGameResponse> getFixturePostGame(@PathVariable UUID fixtureId) {
        return ResponseEntity.ok(postGameStatsService.getSummaryByFixture(fixtureId));
    }

    @GetMapping("/api/v1/fixtures/{fixtureId}/post-game/{teamPlayerId}")
    @Operation(
        summary = "Get post-game stats for a single player in a fixture"
    )
    @ApiResponse(responseCode = "200", description = "Player summary returned")
    @ApiResponse(responseCode = "404", description = "Summary not found for this player/fixture combination")
    public ResponseEntity<PostGameSummaryResponse> getPlayerPostGame(
            @PathVariable UUID fixtureId,
            @PathVariable UUID teamPlayerId) {
        return ResponseEntity.ok(postGameStatsService.getSummaryForPlayer(fixtureId, teamPlayerId));
    }

    @PostMapping("/api/v1/admin/fixtures/{fixtureId}/post-game/recompute")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Manually trigger post-game stats recomputation",
        description = "Recomputes and upserts post-game summaries for all players in the fixture. " +
                      "Useful if stat entries are updated after the automatic computation ran. " +
                      "Runs asynchronously — returns 202 Accepted immediately."
    )
    @ApiResponse(responseCode = "202", description = "Recomputation triggered")
    public ResponseEntity<Void> recomputePostGame(@PathVariable UUID fixtureId) {
        postGameStatsService.computeAndStore(fixtureId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PutMapping("/api/v1/admin/leagues/{leagueId}/fixtures/{fixtureId}/post-game/{teamPlayerId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Field-level update of a player's post-game stats",
        description = "Allows an admin to manually set individual stat values for a specific player " +
                      "in a fixture. Only non-null fields in the request body are applied; omitted " +
                      "fields retain their existing values. Fixture must be IN_PROGRESS or COMPLETED. " +
                      "To clear all custom stats, send \"customStats\": {}."
    )
    @ApiResponse(responseCode = "200", description = "Stats updated successfully")
    @ApiResponse(responseCode = "400", description = "Fixture is not IN_PROGRESS or COMPLETED, or validation error")
    @ApiResponse(responseCode = "404", description = "Fixture or player summary not found")
    public ResponseEntity<PostGameSummaryResponse> updatePostGameStats(
            @PathVariable UUID leagueId,
            @PathVariable UUID fixtureId,
            @PathVariable UUID teamPlayerId,
            @Valid @RequestBody UpdatePostGameStatsRequest request) {
        return ResponseEntity.ok(
                postGameStatsService.updatePostGameStats(fixtureId, teamPlayerId, request));
    }

    @GetMapping("/api/v1/athletes/{athleteId}/post-game-history")
    @Operation(
        summary = "Get paginated post-game history for an athlete",
        description = "Returns a page of post-game summaries across all fixtures the athlete has participated in."
    )
    @ApiResponse(responseCode = "200", description = "History returned")
    public ResponseEntity<Page<PostGameSummaryResponse>> getAthletePostGameHistory(
            @PathVariable UUID athleteId,
            Pageable pageable) {
        return ResponseEntity.ok(postGameStatsService.getSummaryForAthlete(athleteId, pageable));
    }
}
