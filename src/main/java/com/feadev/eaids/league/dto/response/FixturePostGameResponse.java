package com.feadev.eaids.league.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * Post-game breakdown for a completed fixture, with per-athlete stats
 * split into home and away team groups.
 */
@Data
@Builder
public class FixturePostGameResponse {

    private UUID fixtureId;
    private UUID homeTeamId;
    private String homeTeamName;
    private UUID awayTeamId;
    private String awayTeamName;
    private Integer homeScore;
    private Integer awayScore;

    private List<PostGameSummaryResponse> homeTeamStats;
    private List<PostGameSummaryResponse> awayTeamStats;
}
