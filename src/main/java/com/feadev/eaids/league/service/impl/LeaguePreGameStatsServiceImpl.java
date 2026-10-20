package com.feadev.eaids.league.service.impl;

import com.feadev.eaids.league.dto.request.RecordLeagueStatsRequest;
import com.feadev.eaids.league.dto.response.FixturePreGameStatsResponse;
import com.feadev.eaids.league.dto.response.LeagueStatEntryResponse;
import com.feadev.eaids.league.model.LeagueStatEntry;
import com.feadev.eaids.league.model.MatchFixture;
import com.feadev.eaids.league.model.TeamPlayer;
import com.feadev.eaids.league.model.enums.FixtureStatus;
import com.feadev.eaids.league.model.enums.PlayerStatus;
import com.feadev.eaids.league.repository.LeagueStatEntryRepository;
import com.feadev.eaids.league.repository.MatchFixtureRepository;
import com.feadev.eaids.league.repository.TeamPlayerRepository;
import com.feadev.eaids.league.service.LeaguePreGameStatsService;
import com.feadev.eaids.league.service.LeagueStatsAggregator;
import com.feadev.eaids.matchstats.model.MatchStatDefinition;
import com.feadev.eaids.matchstats.repository.MatchStatDefinitionRepository;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LeaguePreGameStatsServiceImpl implements LeaguePreGameStatsService {

    private final MatchFixtureRepository fixtureRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final MatchStatDefinitionRepository definitionRepository;
    private final LeagueStatEntryRepository statEntryRepository;
    private final LeagueStatsAggregator statsAggregator;

    // -----------------------------------------------------------------------
    // Stat recording
    // -----------------------------------------------------------------------

    @Override
    public List<LeagueStatEntryResponse> recordLeagueStats(UUID fixtureId,
                                                           RecordLeagueStatsRequest request) {
        MatchFixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fixture not found with id: " + fixtureId));

        if (fixture.getStatus() != FixtureStatus.IN_PROGRESS
                && fixture.getStatus() != FixtureStatus.COMPLETED) {
            throw new InvalidStateTransitionException(
                    "Stats can only be recorded for IN_PROGRESS or COMPLETED fixtures. " +
                    "Current status: " + fixture.getStatus());
        }

        // Build an index of all team player IDs eligible for this fixture
        UUID homeTeamId = fixture.getHomeTeam().getId();
        UUID awayTeamId = fixture.getAwayTeam().getId();

        List<TeamPlayer> homePlayers = teamPlayerRepository.findByTeamId(homeTeamId);
        List<TeamPlayer> awayPlayers = teamPlayerRepository.findByTeamId(awayTeamId);

        Map<UUID, TeamPlayer> eligiblePlayers = new java.util.HashMap<>();
        homePlayers.forEach(p -> eligiblePlayers.put(p.getId(), p));
        awayPlayers.forEach(p -> eligiblePlayers.put(p.getId(), p));

        // Resolve definitions up front to fail fast on bad IDs
        Set<UUID> definitionIds = request.getEntries().stream()
                .map(RecordLeagueStatsRequest.PlayerStatEntry::getDefinitionId)
                .collect(Collectors.toSet());
        Map<UUID, MatchStatDefinition> definitions = definitionRepository.findAllById(definitionIds)
                .stream()
                .collect(Collectors.toMap(MatchStatDefinition::getId, Function.identity()));

        UUID recordedBy = getCurrentUserId();

        List<LeagueStatEntry> toSave = new ArrayList<>();
        for (RecordLeagueStatsRequest.PlayerStatEntry entry : request.getEntries()) {
            TeamPlayer player = eligiblePlayers.get(entry.getTeamPlayerId());
            if (player == null) {
                throw new ResourceNotFoundException(
                        "TeamPlayer not found or does not belong to either team in this fixture: "
                        + entry.getTeamPlayerId());
            }

            MatchStatDefinition definition = definitions.get(entry.getDefinitionId());
            if (definition == null) {
                throw new ResourceNotFoundException(
                        "Stat definition not found with id: " + entry.getDefinitionId());
            }

            toSave.add(LeagueStatEntry.builder()
                    .teamPlayer(player)
                    .fixture(fixture)
                    .definition(definition)
                    .value(entry.getValue())
                    .recordedBy(recordedBy)
                    .build());
        }

        List<LeagueStatEntry> saved = statEntryRepository.saveAll(toSave);
        log.info("Recorded {} league stat entries for fixture {}", saved.size(), fixtureId);

        return saved.stream()
                .map(this::mapToEntryResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Pre-game stats
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public FixturePreGameStatsResponse getPreGameStats(UUID fixtureId) {
        MatchFixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fixture not found with id: " + fixtureId));

        UUID leagueId = fixture.getLeague().getId();
        UUID homeTeamId = fixture.getHomeTeam().getId();
        UUID awayTeamId = fixture.getAwayTeam().getId();

        // Active rosters for both teams
        List<TeamPlayer> homePlayers = teamPlayerRepository
                .findByTeamIdAndStatus(homeTeamId, PlayerStatus.ACTIVE);
        List<TeamPlayer> awayPlayers = teamPlayerRepository
                .findByTeamIdAndStatus(awayTeamId, PlayerStatus.ACTIVE);

        List<UUID> homePlayerIds = homePlayers.stream()
                .map(TeamPlayer::getId).collect(Collectors.toList());
        List<UUID> awayPlayerIds = awayPlayers.stream()
                .map(TeamPlayer::getId).collect(Collectors.toList());

        // All completed stat entries for each team's players in this league
        List<LeagueStatEntry> homeEntries = homePlayerIds.isEmpty()
                ? List.of()
                : statEntryRepository.findByLeagueIdAndFixtureStatusAndTeamPlayerIdIn(
                        leagueId, FixtureStatus.COMPLETED, homePlayerIds);

        List<LeagueStatEntry> awayEntries = awayPlayerIds.isEmpty()
                ? List.of()
                : statEntryRepository.findByLeagueIdAndFixtureStatusAndTeamPlayerIdIn(
                        leagueId, FixtureStatus.COMPLETED, awayPlayerIds);

        // Completed fixtures for W/L record computation
        List<MatchFixture> homeFixtures = fixtureRepository.findByTeamId(homeTeamId)
                .stream()
                .filter(f -> f.getStatus() == FixtureStatus.COMPLETED)
                .collect(Collectors.toList());

        List<MatchFixture> awayFixtures = fixtureRepository.findByTeamId(awayTeamId)
                .stream()
                .filter(f -> f.getStatus() == FixtureStatus.COMPLETED)
                .collect(Collectors.toList());

        // All entries combined — needed for opponent stats
        List<LeagueStatEntry> allEntries = new ArrayList<>(homeEntries);
        allEntries.addAll(awayEntries);

        FixturePreGameStatsResponse.TeamPreGameStats homeStats =
                statsAggregator.aggregate(homeEntries, allEntries, homePlayers, homeFixtures,
                        homeTeamId, awayTeamId, fixture.getHomeTeam().getName());

        FixturePreGameStatsResponse.TeamPreGameStats awayStats =
                statsAggregator.aggregate(awayEntries, allEntries, awayPlayers, awayFixtures,
                        awayTeamId, homeTeamId, fixture.getAwayTeam().getName());

        return FixturePreGameStatsResponse.builder()
                .fixtureId(fixture.getId())
                .leagueId(leagueId)
                .leagueName(fixture.getLeague().getName())
                .homeTeamStats(homeStats)
                .awayTeamStats(awayStats)
                .scheduledDate(fixture.getScheduledDate())
                .venue(fixture.getVenue())
                .status(fixture.getStatus().name())
                .build();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private LeagueStatEntryResponse mapToEntryResponse(LeagueStatEntry e) {
        TeamPlayer tp = e.getTeamPlayer();
        MatchStatDefinition def = e.getDefinition();
        String playerName = tp.getAthlete() != null ? tp.getAthlete().getEmail() : "Unknown";

        return LeagueStatEntryResponse.builder()
                .id(e.getId())
                .teamPlayerId(tp.getId())
                .playerName(playerName)
                .jerseyNumber(tp.getJerseyNumber())
                .position(tp.getPosition() != null ? tp.getPosition().name() : null)
                .fixtureId(e.getFixture().getId())
                .statName(def.getStatName())
                .unit(def.getUnit())
                .positionGroup(def.getPositionGroup() != null ? def.getPositionGroup().name() : null)
                .value(e.getValue())
                .recordedDate(e.getCreatedDate())
                .build();
    }

    private UUID getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }
}
