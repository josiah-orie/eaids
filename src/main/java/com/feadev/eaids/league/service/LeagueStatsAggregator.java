package com.feadev.eaids.league.service;

import com.feadev.eaids.league.dto.response.FixturePreGameStatsResponse;
import com.feadev.eaids.league.dto.response.FixturePreGameStatsResponse.KeyPlayerStat;
import com.feadev.eaids.league.dto.response.FixturePreGameStatsResponse.TeamPreGameStats;
import com.feadev.eaids.league.dto.response.FixturePreGameStatsResponse.TeamRecord;
import com.feadev.eaids.league.model.LeagueStatEntry;
import com.feadev.eaids.league.model.MatchFixture;
import com.feadev.eaids.league.model.TeamPlayer;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Stateless aggregation component that computes pre-game team and player stats
 * from a set of {@link LeagueStatEntry} records.
 *
 * <p>All averages are rounded to 2 decimal places (HALF_UP).
 * When no data exists, all numeric fields are zero and keyPlayers is empty.
 */
@Component
public class LeagueStatsAggregator {

    // Stat name constants matching the seeded MatchStatDefinition records
    private static final String STAT_PASSING_YARDS   = "Passing Yards";
    private static final String STAT_RUSHING_YARDS   = "Rushing Yards";
    private static final String STAT_RECEIVING_YARDS = "Receiving Yards";
    private static final String STAT_TACKLES         = "Tackles";

    private static final Set<FootballPosition> DEFENSIVE_POSITIONS = Set.of(
            FootballPosition.LINEBACKER,
            FootballPosition.DEFENSIVE_LINE,
            FootballPosition.CORNERBACK,
            FootballPosition.SAFETY
    );

    /**
     * Aggregate stats for one team.
     *
     * @param teamEntries        stat entries belonging to this team's players only
     * @param allEntries         combined entries for both teams (used to derive opponent stats)
     * @param teamPlayers        active roster for this team
     * @param completedFixtures  all completed fixtures involving this team (for W/L + scoring)
     * @param teamId             this team's ID
     * @param opponentTeamId     the opposing team's ID (used to derive opponent stats)
     * @param teamName           display name for this team
     */
    public TeamPreGameStats aggregate(
            List<LeagueStatEntry> teamEntries,
            List<LeagueStatEntry> allEntries,
            List<TeamPlayer> teamPlayers,
            List<MatchFixture> completedFixtures,
            UUID teamId,
            UUID opponentTeamId,
            String teamName) {

        int gamesPlayed = completedFixtures.size();

        // --- W/L record ---
        TeamRecord record = computeRecord(completedFixtures, teamId);

        // --- Scoring totals and averages ---
        BigDecimal totalPoints = computeTotalPoints(completedFixtures, teamId);
        BigDecimal totalOpponentPoints = computeTotalPoints(completedFixtures, opponentTeamId);

        BigDecimal pointsPerGame = average(totalPoints, gamesPlayed);
        BigDecimal opponentPointsPerGame = average(totalOpponentPoints, gamesPlayed);

        // --- Yardage averages from stat entries ---
        Set<UUID> teamPlayerIds = teamPlayers.stream()
                .map(TeamPlayer::getId)
                .collect(Collectors.toSet());

        Set<UUID> opponentPlayerIds = allEntries.stream()
                .map(e -> e.getTeamPlayer().getId())
                .filter(id -> !teamPlayerIds.contains(id))
                .collect(Collectors.toSet());

        // Distinct fixture IDs with at least one entry — used as denominator for yard averages
        Set<UUID> fixturesWithEntries = teamEntries.stream()
                .map(e -> e.getFixture().getId())
                .collect(Collectors.toSet());
        int yardGames = fixturesWithEntries.isEmpty() ? gamesPlayed : fixturesWithEntries.size();

        BigDecimal totalPassingYards   = sumByStat(teamEntries, STAT_PASSING_YARDS);
        BigDecimal totalRushingYards   = sumByStat(teamEntries, STAT_RUSHING_YARDS);

        BigDecimal passingYardsPerGame = average(totalPassingYards, yardGames);
        BigDecimal rushingYardsPerGame = average(totalRushingYards, yardGames);

        // Opponent yardage — from entries belonging to the opponent's players
        List<LeagueStatEntry> opponentEntries = allEntries.stream()
                .filter(e -> opponentPlayerIds.contains(e.getTeamPlayer().getId()))
                .collect(Collectors.toList());

        Set<UUID> opponentFixturesWithEntries = opponentEntries.stream()
                .map(e -> e.getFixture().getId())
                .collect(Collectors.toSet());
        int oppYardGames = opponentFixturesWithEntries.isEmpty() ? gamesPlayed
                : opponentFixturesWithEntries.size();

        BigDecimal opponentPassingYardsPerGame = average(
                sumByStat(opponentEntries, STAT_PASSING_YARDS), oppYardGames);
        BigDecimal opponentRushingYardsPerGame = average(
                sumByStat(opponentEntries, STAT_RUSHING_YARDS), oppYardGames);

        // --- Key players ---
        Map<UUID, TeamPlayer> playerIndex = teamPlayers.stream()
                .collect(Collectors.toMap(TeamPlayer::getId, p -> p));

        List<KeyPlayerStat> keyPlayers = buildKeyPlayers(teamEntries, playerIndex);

        return TeamPreGameStats.builder()
                .teamId(teamId)
                .teamName(teamName)
                .record(record)
                .gamesPlayed(gamesPlayed)
                .pointsPerGame(pointsPerGame)
                .passingYardsPerGame(passingYardsPerGame)
                .rushingYardsPerGame(rushingYardsPerGame)
                .totalPoints(totalPoints)
                .totalPassingYards(totalPassingYards)
                .totalRushingYards(totalRushingYards)
                .opponentPointsPerGame(opponentPointsPerGame)
                .opponentPassingYardsPerGame(opponentPassingYardsPerGame)
                .opponentRushingYardsPerGame(opponentRushingYardsPerGame)
                .keyPlayers(keyPlayers)
                .build();
    }

    // -----------------------------------------------------------------------
    // Record computation
    // -----------------------------------------------------------------------

    private TeamRecord computeRecord(List<MatchFixture> fixtures, UUID teamId) {
        int wins = 0;
        int losses = 0;
        for (MatchFixture f : fixtures) {
            if (f.getHomeScore() == null || f.getAwayScore() == null) {
                continue;
            }
            boolean isHome = f.getHomeTeam().getId().equals(teamId);
            int teamScore     = isHome ? f.getHomeScore() : f.getAwayScore();
            int opponentScore = isHome ? f.getAwayScore() : f.getHomeScore();
            if (teamScore > opponentScore) {
                wins++;
            } else if (teamScore < opponentScore) {
                losses++;
            }
            // draws are counted as neither win nor loss
        }
        return new TeamRecord(wins, losses);
    }

    // -----------------------------------------------------------------------
    // Scoring totals
    // -----------------------------------------------------------------------

    private BigDecimal computeTotalPoints(List<MatchFixture> fixtures, UUID teamId) {
        int total = 0;
        for (MatchFixture f : fixtures) {
            if (f.getHomeScore() == null || f.getAwayScore() == null) {
                continue;
            }
            boolean isHome = f.getHomeTeam().getId().equals(teamId);
            total += isHome ? f.getHomeScore() : f.getAwayScore();
        }
        return BigDecimal.valueOf(total);
    }

    // -----------------------------------------------------------------------
    // Yardage helpers
    // -----------------------------------------------------------------------

    /** Sum of all entry values where the stat name matches. */
    private BigDecimal sumByStat(List<LeagueStatEntry> entries, String statName) {
        return entries.stream()
                .filter(e -> statName.equalsIgnoreCase(e.getDefinition().getStatName()))
                .map(LeagueStatEntry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal average(BigDecimal total, int games) {
        if (games == 0 || total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return total.divide(BigDecimal.valueOf(games), 2, RoundingMode.HALF_UP);
    }

    // -----------------------------------------------------------------------
    // Key player selection
    // -----------------------------------------------------------------------

    /**
     * Select top performer per position group:
     * <ul>
     *   <li>QB  — highest total Passing Yards</li>
     *   <li>RB  — highest total Rushing Yards (RUNNING_BACK)</li>
     *   <li>WR  — highest total Receiving Yards (WIDE_RECEIVER)</li>
     *   <li>DEF — highest total Tackles (LINEBACKER / DEFENSIVE_LINE / CORNERBACK / SAFETY)</li>
     * </ul>
     * Only includes a slot when the player has at least one relevant stat entry.
     */
    private List<KeyPlayerStat> buildKeyPlayers(
            List<LeagueStatEntry> entries,
            Map<UUID, TeamPlayer> playerIndex) {

        List<KeyPlayerStat> result = new ArrayList<>();

        topPlayer(entries, playerIndex, FootballPosition.QUARTERBACK,
                  STAT_PASSING_YARDS).ifPresent(result::add);

        topPlayer(entries, playerIndex, FootballPosition.RUNNING_BACK,
                  STAT_RUSHING_YARDS).ifPresent(result::add);

        topPlayer(entries, playerIndex, FootballPosition.WIDE_RECEIVER,
                  STAT_RECEIVING_YARDS).ifPresent(result::add);

        topDefender(entries, playerIndex).ifPresent(result::add);

        return result;
    }

    private Optional<KeyPlayerStat> topPlayer(
            List<LeagueStatEntry> entries,
            Map<UUID, TeamPlayer> playerIndex,
            FootballPosition position,
            String statName) {

        // Entries matching this position + stat name
        Map<UUID, List<LeagueStatEntry>> byPlayer = entries.stream()
                .filter(e -> {
                    TeamPlayer tp = playerIndex.get(e.getTeamPlayer().getId());
                    return tp != null
                            && tp.getPosition() == position
                            && statName.equalsIgnoreCase(e.getDefinition().getStatName());
                })
                .collect(Collectors.groupingBy(e -> e.getTeamPlayer().getId()));

        return byPlayer.entrySet().stream()
                .map(entry -> {
                    UUID playerId = entry.getKey();
                    List<LeagueStatEntry> playerEntries = entry.getValue();
                    BigDecimal total = playerEntries.stream()
                            .map(LeagueStatEntry::getValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    int games = (int) playerEntries.stream()
                            .map(e -> e.getFixture().getId())
                            .distinct()
                            .count();
                    TeamPlayer tp = playerIndex.get(playerId);
                    String name = tp.getAthlete() != null
                            ? tp.getAthlete().getEmail() : "Unknown";
                    return KeyPlayerStat.builder()
                            .playerName(name)
                            .jerseyNumber(tp.getJerseyNumber())
                            .position(tp.getPosition().name())
                            .statName(statName)
                            .total(total)
                            .gamesPlayed(games)
                            .build();
                })
                .max(Comparator.comparing(KeyPlayerStat::getTotal));
    }

    private Optional<KeyPlayerStat> topDefender(
            List<LeagueStatEntry> entries,
            Map<UUID, TeamPlayer> playerIndex) {

        Map<UUID, List<LeagueStatEntry>> byPlayer = entries.stream()
                .filter(e -> {
                    TeamPlayer tp = playerIndex.get(e.getTeamPlayer().getId());
                    return tp != null
                            && DEFENSIVE_POSITIONS.contains(tp.getPosition())
                            && STAT_TACKLES.equalsIgnoreCase(e.getDefinition().getStatName());
                })
                .collect(Collectors.groupingBy(e -> e.getTeamPlayer().getId()));

        return byPlayer.entrySet().stream()
                .map(entry -> {
                    UUID playerId = entry.getKey();
                    List<LeagueStatEntry> playerEntries = entry.getValue();
                    BigDecimal total = playerEntries.stream()
                            .map(LeagueStatEntry::getValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    int games = (int) playerEntries.stream()
                            .map(e -> e.getFixture().getId())
                            .distinct()
                            .count();
                    TeamPlayer tp = playerIndex.get(playerId);
                    String name = tp.getAthlete() != null
                            ? tp.getAthlete().getEmail() : "Unknown";
                    return KeyPlayerStat.builder()
                            .playerName(name)
                            .jerseyNumber(tp.getJerseyNumber())
                            .position(tp.getPosition().name())
                            .statName(STAT_TACKLES)
                            .total(total)
                            .gamesPlayed(games)
                            .build();
                })
                .max(Comparator.comparing(KeyPlayerStat::getTotal));
    }
}
