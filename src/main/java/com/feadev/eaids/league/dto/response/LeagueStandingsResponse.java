package com.feadev.eaids.league.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueStandingsResponse {

    private UUID leagueId;
    private String leagueName;
    private int seasonYear;

    /**
     * One entry per division, ordered by tierRank ascending (Division 1 first).
     * When a {@code divisionId} filter is applied only one entry is present.
     * Fixtures not tagged to any division are omitted from the per-division tables.
     */
    private List<DivisionStandings> divisions;

    // -----------------------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DivisionStandings {

        private UUID divisionId;
        private String divisionName;
        private int tierRank;

        /** Teams ranked by wins DESC, pointsDifferential DESC, pointsFor DESC. */
        private List<TeamStandingEntry> table;
    }

    // -----------------------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamStandingEntry {

        /** 1-based rank within this division table. */
        private int position;

        private UUID teamId;
        private String teamName;

        private int played;
        private int wins;
        private int draws;
        private int losses;

        private int pointsFor;
        private int pointsAgainst;
        private int pointsDifferential;

        /**
         * Wins / played, rounded to 2 decimal places.
         * Returns 0.00 when no games have been played.
         */
        private double winPercentage;
    }
}
