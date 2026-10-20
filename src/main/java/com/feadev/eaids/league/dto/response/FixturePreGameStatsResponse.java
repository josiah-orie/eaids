package com.feadev.eaids.league.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FixturePreGameStatsResponse {

    private UUID fixtureId;
    private UUID leagueId;
    private String leagueName;
    private LocalDateTime scheduledDate;
    private String venue;
    private String status;
    private TeamPreGameStats homeTeamStats;
    private TeamPreGameStats awayTeamStats;

    // -----------------------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamPreGameStats {

        private UUID teamId;
        private String teamName;

        /** W/L record derived from completed fixtures in this league. */
        private TeamRecord record;

        private int gamesPlayed;

        // Per-game averages — team's own output
        private BigDecimal pointsPerGame;
        private BigDecimal passingYardsPerGame;
        private BigDecimal rushingYardsPerGame;

        // Season totals
        private BigDecimal totalPoints;
        private BigDecimal totalPassingYards;
        private BigDecimal totalRushingYards;

        // Opponent averages — what this team allows per game
        private BigDecimal opponentPointsPerGame;
        private BigDecimal opponentPassingYardsPerGame;
        private BigDecimal opponentRushingYardsPerGame;

        /** Top performer per position group (QB, RB, WR, Defender). */
        private List<KeyPlayerStat> keyPlayers;
    }

    // -----------------------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamRecord {
        private int wins;
        private int losses;
    }

    // -----------------------------------------------------------------------

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KeyPlayerStat {

        /** Athlete email used as display name (User entity has no separate fullName). */
        private String playerName;
        private Integer jerseyNumber;
        private String position;
        private String statName;
        private BigDecimal total;
        private int gamesPlayed;
    }
}
