package com.feadev.eaids.league.service.impl;

import com.feadev.eaids.league.dto.request.CreateFixtureRequest;
import com.feadev.eaids.league.dto.request.TransitionFixtureRequest;
import com.feadev.eaids.league.dto.request.UpdateFixtureRequest;
import com.feadev.eaids.league.dto.response.MatchFixtureResponse;
import com.feadev.eaids.league.model.League;
import com.feadev.eaids.league.model.LeagueDivision;
import com.feadev.eaids.league.model.MatchFixture;
import com.feadev.eaids.league.model.Team;
import com.feadev.eaids.league.model.enums.FixtureStatus;
import com.feadev.eaids.league.repository.LeagueDivisionRepository;
import com.feadev.eaids.league.repository.LeagueRepository;
import com.feadev.eaids.league.repository.MatchFixtureRepository;
import com.feadev.eaids.league.repository.TeamRepository;
import com.feadev.eaids.league.event.FixtureCompletedEvent;
import com.feadev.eaids.league.service.MatchFixtureService;
import com.feadev.eaids.matchstats.model.MatchStatEntry;
import com.feadev.eaids.matchstats.repository.MatchStatEntryRepository;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MatchFixtureServiceImpl implements MatchFixtureService {

    private final MatchFixtureRepository fixtureRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueDivisionRepository divisionRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final MatchStatEntryRepository statEntryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    private static final Map<FixtureStatus, Set<FixtureStatus>> VALID_TRANSITIONS = Map.of(
            FixtureStatus.SCHEDULED, Set.of(FixtureStatus.IN_PROGRESS, FixtureStatus.POSTPONED, FixtureStatus.CANCELLED),
            FixtureStatus.IN_PROGRESS, Set.of(FixtureStatus.COMPLETED),
            FixtureStatus.POSTPONED, Set.of(FixtureStatus.SCHEDULED, FixtureStatus.CANCELLED),
            FixtureStatus.COMPLETED, Set.of(),
            FixtureStatus.CANCELLED, Set.of()
    );

    @Override
    public MatchFixtureResponse createFixture(UUID leagueId, CreateFixtureRequest request) {
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League not found with id: " + leagueId));

        Team homeTeam = teamRepository.findById(request.getHomeTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Home team not found with id: " + request.getHomeTeamId()));

        Team awayTeam = teamRepository.findById(request.getAwayTeamId())
                .orElseThrow(() -> new ResourceNotFoundException("Away team not found with id: " + request.getAwayTeamId()));

        if (request.getHomeTeamId().equals(request.getAwayTeamId())) {
            throw new InvalidStateTransitionException("Home and away teams cannot be the same");
        }

        if (!homeTeam.getLeague().getId().equals(leagueId)) {
            throw new InvalidStateTransitionException(
                    "Home team '" + homeTeam.getName() + "' does not belong to this league");
        }
        if (!awayTeam.getLeague().getId().equals(leagueId)) {
            throw new InvalidStateTransitionException(
                    "Away team '" + awayTeam.getName() + "' does not belong to this league");
        }

        LeagueDivision division = null;
        if (request.getDivisionId() != null) {
            division = divisionRepository.findById(request.getDivisionId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Division not found with id: " + request.getDivisionId()));
            if (!division.getLeague().getId().equals(leagueId)) {
                throw new InvalidStateTransitionException(
                        "Division does not belong to this league");
            }
        }

        if (request.getRefereeUserId() != null && !userRepository.existsById(request.getRefereeUserId())) {
            throw new ResourceNotFoundException("Referee user not found with id: " + request.getRefereeUserId());
        }

        MatchFixture fixture = MatchFixture.builder()
                .league(league)
                .division(division)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .scheduledDate(request.getScheduledDate())
                .venue(request.getVenue())
                .status(FixtureStatus.SCHEDULED)
                .refereeUserId(request.getRefereeUserId())
                .notes(request.getNotes())
                .build();

        MatchFixture saved = fixtureRepository.save(fixture);
        log.info("Fixture created: {} vs {} on {} (league={})",
                homeTeam.getName(), awayTeam.getName(), request.getScheduledDate(), leagueId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MatchFixtureResponse getFixture(UUID fixtureId) {
        return mapToResponse(findFixtureOrThrow(fixtureId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchFixtureResponse> getFixturesByLeague(UUID leagueId, FixtureStatus status, UUID divisionId) {
        if (!leagueRepository.existsById(leagueId)) {
            throw new ResourceNotFoundException("League not found with id: " + leagueId);
        }
        List<MatchFixture> fixtures;
        if (status != null && divisionId != null) {
            fixtures = fixtureRepository.findByLeagueIdAndDivisionId(leagueId, divisionId).stream()
                    .filter(f -> f.getStatus() == status)
                    .collect(Collectors.toList());
        } else if (status != null) {
            fixtures = fixtureRepository.findByLeagueIdAndStatus(leagueId, status);
        } else if (divisionId != null) {
            fixtures = fixtureRepository.findByLeagueIdAndDivisionId(leagueId, divisionId);
        } else {
            fixtures = fixtureRepository.findByLeagueId(leagueId);
        }
        return fixtures.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchFixtureResponse> getFixturesByTeam(UUID teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new ResourceNotFoundException("Team not found with id: " + teamId);
        }
        return fixtureRepository.findByTeamId(teamId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MatchFixtureResponse updateFixture(UUID fixtureId, UpdateFixtureRequest request) {
        MatchFixture fixture = findFixtureOrThrow(fixtureId);

        if (fixture.getStatus() != FixtureStatus.SCHEDULED && fixture.getStatus() != FixtureStatus.POSTPONED) {
            throw new InvalidStateTransitionException(
                    "Fixture can only be updated when SCHEDULED or POSTPONED. Current status: " + fixture.getStatus());
        }

        if (request.getScheduledDate() != null) fixture.setScheduledDate(request.getScheduledDate());
        if (request.getVenue() != null) fixture.setVenue(request.getVenue());
        if (request.getRefereeUserId() != null) {
            if (!userRepository.existsById(request.getRefereeUserId())) {
                throw new ResourceNotFoundException("Referee user not found with id: " + request.getRefereeUserId());
            }
            fixture.setRefereeUserId(request.getRefereeUserId());
        }
        if (request.getNotes() != null) fixture.setNotes(request.getNotes());

        MatchFixture saved = fixtureRepository.save(fixture);
        log.info("Fixture updated: {}", fixtureId);
        return mapToResponse(saved);
    }

    @Override
    public MatchFixtureResponse transitionStatus(UUID fixtureId, TransitionFixtureRequest request) {
        MatchFixture fixture = findFixtureOrThrow(fixtureId);
        FixtureStatus current = fixture.getStatus();
        FixtureStatus newStatus = request.getStatus();

        Set<FixtureStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidStateTransitionException(
                    String.format("Cannot transition fixture from %s to %s. Allowed transitions: %s",
                            current, newStatus, allowed));
        }

        if (newStatus == FixtureStatus.COMPLETED) {
            if (request.getHomeScore() == null || request.getAwayScore() == null) {
                throw new InvalidStateTransitionException(
                        "Home score and away score are required to complete a fixture");
            }
            fixture.setHomeScore(request.getHomeScore());
            fixture.setAwayScore(request.getAwayScore());
        }

        fixture.setStatus(newStatus);
        MatchFixture saved = fixtureRepository.save(fixture);
        log.info("Fixture {} transitioned from {} to {}", fixtureId, current, newStatus);

        // Trigger async post-game snapshot computation when the fixture completes.
        // The event listener fires after this transaction commits, so the async thread
        // always reads fully committed data.
        if (newStatus == FixtureStatus.COMPLETED) {
            applicationEventPublisher.publishEvent(
                    new FixtureCompletedEvent(fixtureId, fixture.getLeague().getId()));
        }

        return mapToResponse(saved);
    }

    @Override
    public void deleteFixture(UUID fixtureId) {
        MatchFixture fixture = findFixtureOrThrow(fixtureId);

        if (fixture.getStatus() != FixtureStatus.SCHEDULED && fixture.getStatus() != FixtureStatus.POSTPONED) {
            throw new InvalidStateTransitionException(
                    "Only SCHEDULED or POSTPONED fixtures can be deleted. Current status: " + fixture.getStatus());
        }

        fixtureRepository.delete(fixture);
        log.info("Fixture deleted: {}", fixtureId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatchStatEntry> getFixtureStats(UUID fixtureId) {
        if (!fixtureRepository.existsById(fixtureId)) {
            throw new ResourceNotFoundException("Fixture not found with id: " + fixtureId);
        }
        return statEntryRepository.findByFixtureId(fixtureId);
    }

    private MatchFixture findFixtureOrThrow(UUID fixtureId) {
        return fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new ResourceNotFoundException("Fixture not found with id: " + fixtureId));
    }

    private MatchFixtureResponse mapToResponse(MatchFixture f) {
        return MatchFixtureResponse.builder()
                .id(f.getId())
                .leagueId(f.getLeague().getId())
                .leagueName(f.getLeague().getName())
                .divisionId(f.getDivision() != null ? f.getDivision().getId() : null)
                .divisionName(f.getDivision() != null ? f.getDivision().getName() : null)
                .homeTeamId(f.getHomeTeam().getId())
                .homeTeamName(f.getHomeTeam().getName())
                .awayTeamId(f.getAwayTeam().getId())
                .awayTeamName(f.getAwayTeam().getName())
                .scheduledDate(f.getScheduledDate())
                .venue(f.getVenue())
                .status(f.getStatus().name())
                .homeScore(f.getHomeScore())
                .awayScore(f.getAwayScore())
                .refereeUserId(f.getRefereeUserId())
                .notes(f.getNotes())
                .createdDate(f.getCreatedDate())
                .lastModifiedDate(f.getLastModifiedDate())
                .createdBy(f.getCreatedBy())
                .lastModifiedBy(f.getLastModifiedBy())
                .build();
    }
}
