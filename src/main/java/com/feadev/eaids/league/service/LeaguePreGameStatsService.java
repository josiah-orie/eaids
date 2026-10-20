package com.feadev.eaids.league.service;

import com.feadev.eaids.league.dto.request.RecordLeagueStatsRequest;
import com.feadev.eaids.league.dto.response.FixturePreGameStatsResponse;
import com.feadev.eaids.league.dto.response.LeagueStatEntryResponse;

import java.util.List;
import java.util.UUID;

public interface LeaguePreGameStatsService {

    /**
     * Record a batch of player stats for a fixture.
     * Fixture must be IN_PROGRESS or COMPLETED.
     * Each teamPlayerId must belong to one of the fixture's two teams.
     */
    List<LeagueStatEntryResponse> recordLeagueStats(UUID fixtureId, RecordLeagueStatsRequest request);

    /**
     * Build the pre-game statistical breakdown for both teams in a fixture.
     * Aggregates LeagueStatEntry records from completed fixtures in the same league.
     * Returns zeroed stats when no historical data exists — never throws 404 for missing stats.
     */
    FixturePreGameStatsResponse getPreGameStats(UUID fixtureId);
}
