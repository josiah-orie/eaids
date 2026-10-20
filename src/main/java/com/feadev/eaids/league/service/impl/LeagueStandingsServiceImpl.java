package com.feadev.eaids.league.service.impl;

import com.feadev.eaids.league.dto.response.LeagueStandingsResponse;
import com.feadev.eaids.league.dto.response.LeagueStandingsResponse.DivisionStandings;
import com.feadev.eaids.league.dto.response.LeagueStandingsResponse.TeamStandingEntry;
import com.feadev.eaids.league.model.League;
import com.feadev.eaids.league.model.LeagueDivision;
import com.feadev.eaids.league.model.MatchFixture;
import com.feadev.eaids.league.model.Team;
import com.feadev.eaids.league.model.enums.FixtureStatus;
import com.feadev.eaids.league.repository.LeagueDivisionRepository;
import com.feadev.eaids.league.repository.LeagueRepository;
import com.feadev.eaids.league.repository.MatchFixtureRepository;
import com.feadev.eaids.league.repository.TeamRepository;
import com.feadev.eaids.league.service.LeagueStandingsService;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LeagueStandingsServiceImpl implements LeagueStandingsService {

    private final LeagueRepository leagueRepository;
    private final LeagueDivisionRepository divisionRepository;
    private final TeamRepository teamRepository;
    private final MatchFixtureRepository fixtureRepository;

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    @Override
    public LeagueStandingsResponse getStandings(UUID leagueId) {
        League league = findLeagueOrThrow(leagueId);

        List<LeagueDivision> divisions =
                divisionRepository.findByLeagueIdOrderByTierRank(leagueId);

        List<DivisionStandings> divisionStandings = divisions.stream()
                .map(div -> buildDivisionStandings(league, div))
                .collect(Collectors.toList());

        return LeagueStandingsResponse.builder()
                .leagueId(league.getId())
                .leagueName(league.getName())
                .seasonYear(league.getSeasonYear())
                .divisions(divisionStandings)
                .build();
    }

    @Override
    public LeagueStandingsResponse getStandingsByDivision(UUID leagueId, UUID divisionId) {
        League league = findLeagueOrThrow(leagueId);

        LeagueDivision division = divisionRepository.findById(divisionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Division not found with id: " + divisionId));

        if (!division.getLeague().getId().equals(leagueId)) {
            throw new ResourceNotFoundException(
                    "Division does not belong to league: " + leagueId);
        }

        DivisionStandings divStandings = buildDivisionStandings(league, division);

        return LeagueStandingsResponse.builder()
                .leagueId(league.getId())
                .leagueName(league.getName())
                .seasonYear(league.getSeasonYear())
                .divisions(List.of(divStandings))
                .build();
    }

    // -----------------------------------------------------------------------
    // Core computation
    // -----------------------------------------------------------------------

    /**
     * Build the standing table for one division.
     *
     * <p>Teams are seeded from {@code team.division} assignment.
     * Results are counted only from COMPLETED fixtures whose {@code fixture.division_id}
     * matches this division — cross-division fixtures tagged elsewhere are excluded.
     */
    private DivisionStandings buildDivisionStandings(League league, LeagueDivision division) {
        // 1. All teams assigned to this division
        List<Team> teams = teamRepository.findByDivisionId(division.getId());

        // 2. All COMPLETED fixtures tagged to this division
        List<MatchFixture> completed = fixtureRepository
                .findByLeagueIdAndDivisionIdAndStatus(
                        league.getId(), division.getId(), FixtureStatus.COMPLETED);

        // 3. Build a mutable stats accumulator per team
        Map<UUID, MutableStats> statsMap = new HashMap<>();
        for (Team t : teams) {
            statsMap.put(t.getId(), new MutableStats(t.getId(), t.getName()));
        }

        // 4. Walk every completed fixture and credit/debit each team's row
        for (MatchFixture f : completed) {
            if (f.getHomeScore() == null || f.getAwayScore() == null) {
                continue; // defensive guard — should not happen for COMPLETED fixtures
            }

            UUID homeId = f.getHomeTeam().getId();
            UUID awayId = f.getAwayTeam().getId();
            int homeScore = f.getHomeScore();
            int awayScore = f.getAwayScore();

            // Only credit teams that are actually in this division's roster
            MutableStats homeStats = statsMap.get(homeId);
            MutableStats awayStats = statsMap.get(awayId);

            if (homeStats != null) {
                homeStats.played++;
                homeStats.pointsFor += homeScore;
                homeStats.pointsAgainst += awayScore;
                if (homeScore > awayScore)       homeStats.wins++;
                else if (homeScore < awayScore)  homeStats.losses++;
                else                             homeStats.draws++;
            }

            if (awayStats != null) {
                awayStats.played++;
                awayStats.pointsFor += awayScore;
                awayStats.pointsAgainst += homeScore;
                if (awayScore > homeScore)       awayStats.wins++;
                else if (awayScore < homeScore)  awayStats.losses++;
                else                             awayStats.draws++;
            }
        }

        // 5. Sort: wins DESC → pointsDifferential DESC → pointsFor DESC → teamName ASC
        List<MutableStats> sorted = statsMap.values().stream()
                .sorted(Comparator
                        .comparingInt(MutableStats::getWins).reversed()
                        .thenComparingInt(MutableStats::getPointsDifferential).reversed()
                        .thenComparingInt(MutableStats::getPointsFor).reversed()
                        .thenComparing(s -> s.teamName))
                .collect(Collectors.toList());

        // 6. Assign 1-based positions and convert to DTOs
        List<TeamStandingEntry> ranked = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            ranked.add(sorted.get(i).toEntry(i + 1));
        }

        return DivisionStandings.builder()
                .divisionId(division.getId())
                .divisionName(division.getName())
                .tierRank(division.getTierRank())
                .table(ranked)
                .build();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private League findLeagueOrThrow(UUID leagueId) {
        return leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "League not found with id: " + leagueId));
    }

    // -----------------------------------------------------------------------
    // Mutable accumulator — used only inside this service
    // -----------------------------------------------------------------------

    private static class MutableStats {
        final UUID teamId;
        final String teamName;
        int played;
        int wins;
        int draws;
        int losses;
        int pointsFor;
        int pointsAgainst;

        MutableStats(UUID teamId, String teamName) {
            this.teamId   = teamId;
            this.teamName = teamName;
        }

        int getWins()               { return wins; }
        int getPointsDifferential() { return pointsFor - pointsAgainst; }
        int getPointsFor()          { return pointsFor; }

        double winPercentage() {
            return played == 0 ? 0.0
                    : Math.round((wins / (double) played) * 100.0) / 100.0;
        }

        TeamStandingEntry toEntry(int position) {
            return TeamStandingEntry.builder()
                    .position(position)
                    .teamId(teamId)
                    .teamName(teamName)
                    .played(played)
                    .wins(wins)
                    .draws(draws)
                    .losses(losses)
                    .pointsFor(pointsFor)
                    .pointsAgainst(pointsAgainst)
                    .pointsDifferential(pointsFor - pointsAgainst)
                    .winPercentage(winPercentage())
                    .build();
        }
    }
}
