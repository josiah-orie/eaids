package com.feadev.eaids.league.controller;

import com.feadev.eaids.league.dto.response.LeagueStandingsResponse;
import com.feadev.eaids.league.service.LeagueStandingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Read-only league-level endpoints accessible to any authenticated user
 * (scouts, coaches, athletes, admins).
 *
 * <p>Security is enforced at the path level in {@code SecurityConfig}:
 * {@code GET /api/v1/leagues/**} requires authentication.
 */
@RestController
@RequestMapping("/api/v1/leagues")
@RequiredArgsConstructor
@Tag(name = "League Info",
     description = "Read-only league standings for scouts, coaches, and all authenticated users")
public class PublicLeagueController {

    private final LeagueStandingsService standingsService;

    @GetMapping("/{leagueId}/standings")
    @Operation(summary = "Get league standings",
               description = "Returns the standing table for every division in the league, " +
                             "ordered by tier rank. Optionally scope to a single division with " +
                             "?divisionId=. Teams are grouped by their assigned division; " +
                             "results are counted only from COMPLETED fixtures tagged to that division. " +
                             "Accessible by any authenticated user.")
    public ResponseEntity<LeagueStandingsResponse> getStandings(
            @PathVariable UUID leagueId,
            @RequestParam(required = false) UUID divisionId) {
        if (divisionId != null) {
            return ResponseEntity.ok(standingsService.getStandingsByDivision(leagueId, divisionId));
        }
        return ResponseEntity.ok(standingsService.getStandings(leagueId));
    }
}
