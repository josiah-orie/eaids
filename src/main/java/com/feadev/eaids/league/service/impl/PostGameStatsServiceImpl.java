package com.feadev.eaids.league.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feadev.eaids.league.dto.request.UpdatePostGameStatsRequest;
import com.feadev.eaids.league.dto.response.FixturePostGameResponse;
import com.feadev.eaids.league.dto.response.PostGameSummaryResponse;
import com.feadev.eaids.league.event.FixtureCompletedEvent;
import com.feadev.eaids.league.model.LeagueStatEntry;
import com.feadev.eaids.league.model.MatchFixture;
import com.feadev.eaids.league.model.PostGameSummary;
import com.feadev.eaids.league.model.TeamPlayer;
import com.feadev.eaids.league.model.enums.FixtureStatus;
import com.feadev.eaids.league.repository.LeagueStatEntryRepository;
import com.feadev.eaids.league.repository.MatchFixtureRepository;
import com.feadev.eaids.league.repository.PostGameSummaryRepository;
import com.feadev.eaids.league.service.PostGameStatsService;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostGameStatsServiceImpl implements PostGameStatsService {

    // Canonical stat names matching MatchStatDefinition seeds
    private static final String PASSING_YARDS   = "Passing Yards";
    private static final String RUSHING_YARDS   = "Rushing Yards";
    private static final String RECEIVING_YARDS = "Receiving Yards";
    private static final String TACKLES         = "Tackles";
    private static final String TOUCHDOWNS      = "Touchdowns";
    private static final String INTERCEPTIONS   = "Interceptions";
    private static final String SACKS           = "Sacks";
    private static final String FUMBLES         = "Fumbles";
    private static final String PENALTIES       = "Penalties";

    private static final Set<String> TYPED_STATS = Set.of(
            PASSING_YARDS, RUSHING_YARDS, RECEIVING_YARDS, TACKLES,
            TOUCHDOWNS, INTERCEPTIONS, SACKS, FUMBLES, PENALTIES
    );

    private final MatchFixtureRepository fixtureRepository;
    private final LeagueStatEntryRepository statEntryRepository;
    private final PostGameSummaryRepository summaryRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void computeAndStore(UUID fixtureId) {
        log.info("Post-game stats computation started for fixture {}", fixtureId);

        MatchFixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new ResourceNotFoundException("Fixture not found: " + fixtureId));

        List<LeagueStatEntry> entries = statEntryRepository.findByFixtureId(fixtureId);
        if (entries.isEmpty()) {
            log.warn("Post-game computation skipped for fixture {} — no LeagueStatEntry rows found. " +
                     "Stats must be recorded via the league stat entry API before or during the game.", fixtureId);
            return;
        }

        // Group entries by TeamPlayer
        Map<UUID, List<LeagueStatEntry>> byPlayer = entries.stream()
                .collect(Collectors.groupingBy(e -> e.getTeamPlayer().getId()));

        for (Map.Entry<UUID, List<LeagueStatEntry>> playerGroup : byPlayer.entrySet()) {
            UUID teamPlayerId = playerGroup.getKey();
            List<LeagueStatEntry> playerEntries = playerGroup.getValue();
            TeamPlayer teamPlayer = playerEntries.get(0).getTeamPlayer();

            // Aggregate typed stats
            BigDecimal totalPassingYards   = sumByStat(playerEntries, PASSING_YARDS);
            BigDecimal totalRushingYards   = sumByStat(playerEntries, RUSHING_YARDS);
            BigDecimal totalReceivingYards = sumByStat(playerEntries, RECEIVING_YARDS);
            BigDecimal totalTackles        = sumByStat(playerEntries, TACKLES);
            BigDecimal touchdowns          = sumByStat(playerEntries, TOUCHDOWNS);
            BigDecimal interceptions       = sumByStat(playerEntries, INTERCEPTIONS);
            BigDecimal sacks               = sumByStat(playerEntries, SACKS);
            BigDecimal fumbles             = sumByStat(playerEntries, FUMBLES);
            BigDecimal penalties           = sumByStat(playerEntries, PENALTIES);

            // Custom stats — anything not in the typed set
            Map<String, BigDecimal> customMap = new LinkedHashMap<>();
            for (LeagueStatEntry entry : playerEntries) {
                String statName = entry.getDefinition().getStatName();
                if (!TYPED_STATS.contains(statName)) {
                    customMap.merge(statName, entry.getValue(), BigDecimal::add);
                }
            }
            String customStatsJson = serializeCustomStats(customMap);

            // Upsert pattern — load existing or create new
            Optional<PostGameSummary> existing =
                    summaryRepository.findByFixtureIdAndTeamPlayerId(fixtureId, teamPlayerId);

            PostGameSummary summary = existing.orElseGet(PostGameSummary::new);
            summary.setFixture(fixture);
            summary.setTeamPlayer(teamPlayer);
            summary.setAthleteUserId(teamPlayer.getAthlete().getId());
            summary.setTeamId(teamPlayer.getTeam().getId());
            summary.setTotalPassingYards(nullIfZero(totalPassingYards));
            summary.setTotalRushingYards(nullIfZero(totalRushingYards));
            summary.setTotalReceivingYards(nullIfZero(totalReceivingYards));
            summary.setTotalTackles(nullIfZero(totalTackles));
            summary.setTouchdowns(nullIfZero(touchdowns));
            summary.setInterceptions(nullIfZero(interceptions));
            summary.setSacks(nullIfZero(sacks));
            summary.setFumbles(nullIfZero(fumbles));
            summary.setPenalties(nullIfZero(penalties));
            summary.setCustomStats(customStatsJson);
            summary.setComputedAt(LocalDateTime.now());

            summaryRepository.save(summary);
        }

        log.info("Post-game stats computation completed for fixture {} — {} player summaries upserted",
                fixtureId, byPlayer.size());
    }

    /**
     * Listens for {@link FixtureCompletedEvent} and triggers post-game stat computation
     * asynchronously, guaranteed to run only after the fixture transition transaction
     * has fully committed.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void onFixtureCompleted(FixtureCompletedEvent event) {
        log.info("FixtureCompletedEvent received for fixture {} (league {}) — triggering post-game computation",
                event.fixtureId(), event.leagueId());
        computeAndStore(event.fixtureId());
    }

    @Override
    @Transactional(readOnly = true)
    public FixturePostGameResponse getSummaryByFixture(UUID fixtureId) {
        MatchFixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new ResourceNotFoundException("Fixture not found: " + fixtureId));

        List<PostGameSummary> allSummaries = summaryRepository.findByFixtureId(fixtureId);

        UUID homeTeamId = fixture.getHomeTeam().getId();
        UUID awayTeamId = fixture.getAwayTeam().getId();

        List<PostGameSummaryResponse> homeStats = allSummaries.stream()
                .filter(s -> homeTeamId.equals(s.getTeamId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        List<PostGameSummaryResponse> awayStats = allSummaries.stream()
                .filter(s -> awayTeamId.equals(s.getTeamId()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return FixturePostGameResponse.builder()
                .fixtureId(fixtureId)
                .homeTeamId(homeTeamId)
                .homeTeamName(fixture.getHomeTeam().getName())
                .awayTeamId(awayTeamId)
                .awayTeamName(fixture.getAwayTeam().getName())
                .homeScore(fixture.getHomeScore())
                .awayScore(fixture.getAwayScore())
                .homeTeamStats(homeStats)
                .awayTeamStats(awayStats)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PostGameSummaryResponse> getSummaryForAthlete(UUID athleteUserId, Pageable pageable) {
        return summaryRepository.findByAthleteUserId(athleteUserId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PostGameSummaryResponse getSummaryForPlayer(UUID fixtureId, UUID teamPlayerId) {
        PostGameSummary summary = summaryRepository
                .findByFixtureIdAndTeamPlayerId(fixtureId, teamPlayerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post-game summary not found for fixture " + fixtureId +
                        " and player " + teamPlayerId));
        return mapToResponse(summary);
    }

    @Override
    @Transactional
    public PostGameSummaryResponse updatePostGameStats(UUID fixtureId, UUID teamPlayerId,
                                                       UpdatePostGameStatsRequest request) {
        MatchFixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new ResourceNotFoundException("Fixture not found: " + fixtureId));

        if (fixture.getStatus() != FixtureStatus.IN_PROGRESS
                && fixture.getStatus() != FixtureStatus.COMPLETED) {
            throw new InvalidStateTransitionException(
                    "Post-game stats can only be updated when the fixture is IN_PROGRESS or COMPLETED. " +
                    "Current status: " + fixture.getStatus());
        }

        PostGameSummary summary = summaryRepository
                .findByFixtureIdAndTeamPlayerId(fixtureId, teamPlayerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Post-game summary not found for fixture " + fixtureId +
                        " and player " + teamPlayerId));

        // Apply only the non-null fields (partial update)
        if (request.getTotalPassingYards() != null)   summary.setTotalPassingYards(request.getTotalPassingYards());
        if (request.getTotalRushingYards() != null)   summary.setTotalRushingYards(request.getTotalRushingYards());
        if (request.getTotalReceivingYards() != null) summary.setTotalReceivingYards(request.getTotalReceivingYards());
        if (request.getTotalTackles() != null)        summary.setTotalTackles(request.getTotalTackles());
        if (request.getTouchdowns() != null)          summary.setTouchdowns(request.getTouchdowns());
        if (request.getInterceptions() != null)       summary.setInterceptions(request.getInterceptions());
        if (request.getSacks() != null)               summary.setSacks(request.getSacks());
        if (request.getFumbles() != null)             summary.setFumbles(request.getFumbles());
        if (request.getPenalties() != null)           summary.setPenalties(request.getPenalties());

        // customStats: when provided, replace the blob; when null, leave existing intact
        if (request.getCustomStats() != null) {
            summary.setCustomStats(serializeCustomStats(request.getCustomStats()));
        }

        summary.setComputedAt(LocalDateTime.now());

        PostGameSummary saved = summaryRepository.save(summary);
        log.info("Post-game stats manually updated for fixture {} / player {}", fixtureId, teamPlayerId);
        return mapToResponse(saved);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private BigDecimal sumByStat(List<LeagueStatEntry> entries, String statName) {
        return entries.stream()
                .filter(e -> statName.equalsIgnoreCase(e.getDefinition().getStatName()))
                .map(LeagueStatEntry::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal nullIfZero(BigDecimal value) {
        return (value == null || value.compareTo(BigDecimal.ZERO) == 0) ? null : value;
    }

    private String serializeCustomStats(Map<String, BigDecimal> map) {
        if (map == null || map.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize custom stats: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, BigDecimal> deserializeCustomStats(String json) {
        if (json == null || json.isBlank()) return Collections.emptyMap();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, BigDecimal>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize custom stats: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private PostGameSummaryResponse mapToResponse(PostGameSummary s) {
        TeamPlayer tp = s.getTeamPlayer();
        String playerName = (tp != null && tp.getAthlete() != null)
                ? tp.getAthlete().getEmail() : null;
        Integer jerseyNumber = tp != null ? tp.getJerseyNumber() : null;
        String position = (tp != null && tp.getPosition() != null)
                ? tp.getPosition().name() : null;

        return PostGameSummaryResponse.builder()
                .id(s.getId())
                .fixtureId(s.getFixture().getId())
                .teamPlayerId(tp != null ? tp.getId() : null)
                .athleteUserId(s.getAthleteUserId())
                .teamId(s.getTeamId())
                .playerName(playerName)
                .jerseyNumber(jerseyNumber)
                .position(position)
                .totalPassingYards(s.getTotalPassingYards())
                .totalRushingYards(s.getTotalRushingYards())
                .totalReceivingYards(s.getTotalReceivingYards())
                .totalTackles(s.getTotalTackles())
                .touchdowns(s.getTouchdowns())
                .interceptions(s.getInterceptions())
                .sacks(s.getSacks())
                .fumbles(s.getFumbles())
                .penalties(s.getPenalties())
                .customStats(deserializeCustomStats(s.getCustomStats()))
                .computedAt(s.getComputedAt())
                .createdDate(s.getCreatedDate())
                .build();
    }
}
