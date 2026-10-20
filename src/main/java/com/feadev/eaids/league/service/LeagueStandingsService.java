package com.feadev.eaids.league.service;

import com.feadev.eaids.league.dto.response.LeagueStandingsResponse;

import java.util.UUID;

public interface LeagueStandingsService {

    /**
     * Return standings for every division in the league, ordered by tierRank.
     * Each division table contains all teams assigned to that division, ranked by
     * wins DESC, pointsDifferential DESC, pointsFor DESC.
     *
     * <p>Only COMPLETED fixtures whose {@code division_id} matches the division
     * are counted — cross-division fixtures tagged to a different division are excluded.
     *
     * @param leagueId the league to compute standings for
     */
    LeagueStandingsResponse getStandings(UUID leagueId);

    /**
     * Return standings for a single division.
     *
     * @param leagueId   the league the division belongs to
     * @param divisionId the specific division to filter to
     */
    LeagueStandingsResponse getStandingsByDivision(UUID leagueId, UUID divisionId);
}
