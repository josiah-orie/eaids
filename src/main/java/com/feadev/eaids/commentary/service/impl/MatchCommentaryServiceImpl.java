package com.feadev.eaids.commentary.service.impl;

import com.feadev.eaids.commentary.SseEmitterRegistry;
import com.feadev.eaids.commentary.dto.request.PublishCommentaryRequest;
import com.feadev.eaids.commentary.dto.response.CommentaryEventResponse;
import com.feadev.eaids.commentary.model.MatchCommentaryEvent;
import com.feadev.eaids.commentary.model.enums.GameEventType;
import com.feadev.eaids.commentary.repository.MatchCommentaryEventRepository;
import com.feadev.eaids.commentary.service.MatchCommentaryService;
import com.feadev.eaids.league.model.MatchFixture;
import com.feadev.eaids.league.model.TeamPlayer;
import com.feadev.eaids.league.model.enums.FixtureStatus;
import com.feadev.eaids.league.repository.MatchFixtureRepository;
import com.feadev.eaids.league.repository.TeamPlayerRepository;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MatchCommentaryServiceImpl implements MatchCommentaryService {

    private final MatchCommentaryEventRepository commentaryRepository;
    private final MatchFixtureRepository fixtureRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final SseEmitterRegistry sseRegistry;

    @Override
    public CommentaryEventResponse publishEvent(UUID fixtureId,
                                                 PublishCommentaryRequest request,
                                                 UUID actorId) {
        MatchFixture fixture = findFixtureOrThrow(fixtureId);

        if (fixture.getStatus() != FixtureStatus.IN_PROGRESS) {
            throw new IllegalStateException(
                    "Commentary can only be published for IN_PROGRESS fixtures. " +
                    "Current status: " + fixture.getStatus());
        }

        // Resolve optional team player reference
        TeamPlayer teamPlayer = null;
        if (request.getPlayerTeamPlayerId() != null) {
            teamPlayer = teamPlayerRepository.findById(request.getPlayerTeamPlayerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "TeamPlayer not found: " + request.getPlayerTeamPlayerId()));
        }

        // Derive auditor name for the created_by column
        String auditorName = resolveAuditorName(actorId);

        MatchCommentaryEvent event = MatchCommentaryEvent.builder()
                .fixture(fixture)
                .eventType(request.getEventType())
                .eventTimeSeconds(request.getEventTimeSeconds())
                .quarter(request.getQuarter())
                .homeScore(request.getHomeScore())
                .awayScore(request.getAwayScore())
                .description(request.getDescription())
                .playerTeamPlayer(teamPlayer)
                .playerEventSubtype(request.getPlayerEventSubtype())
                .createdBy(auditorName)
                .build();

        MatchCommentaryEvent saved = commentaryRepository.save(event);

        // Side-effect: keep fixture scores in sync on SCORE_UPDATE
        if (request.getEventType() == GameEventType.SCORE_UPDATE) {
            boolean scoreChanged = false;
            if (request.getHomeScore() != null) {
                fixture.setHomeScore(request.getHomeScore());
                scoreChanged = true;
            }
            if (request.getAwayScore() != null) {
                fixture.setAwayScore(request.getAwayScore());
                scoreChanged = true;
            }
            if (scoreChanged) {
                fixtureRepository.save(fixture);
                log.debug("Fixture {} scores updated to {}:{}", fixtureId,
                        fixture.getHomeScore(), fixture.getAwayScore());
            }
        }

        CommentaryEventResponse response = mapToResponse(saved, teamPlayer);
        sseRegistry.broadcast(fixtureId, response);

        log.info("Commentary event published: fixture={} type={} by={}",
                fixtureId, request.getEventType(), actorId);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentaryEventResponse> getRecentEvents(UUID fixtureId) {
        // Fetch last 50 in reverse, then flip to chronological order for replay
        List<MatchCommentaryEvent> events =
                commentaryRepository.findTop50ByFixtureIdOrderByCreatedDateDesc(fixtureId);
        Collections.reverse(events);
        return events.stream().map(e -> mapToResponse(e, e.getPlayerTeamPlayer()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentaryEventResponse> getEventsSince(UUID fixtureId, LocalDateTime since) {
        return commentaryRepository
                .findByFixtureIdAndCreatedDateAfterOrderByCreatedDateAsc(fixtureId, since)
                .stream()
                .map(e -> mapToResponse(e, e.getPlayerTeamPlayer()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentaryEventResponse> getAllEvents(UUID fixtureId) {
        return commentaryRepository.findByFixtureIdOrderByCreatedDateAsc(fixtureId)
                .stream()
                .map(e -> mapToResponse(e, e.getPlayerTeamPlayer()))
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private MatchFixture findFixtureOrThrow(UUID fixtureId) {
        return fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fixture not found: " + fixtureId));
    }

    private String resolveAuditorName(UUID actorId) {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return actorId != null ? actorId.toString() : "system";
        }
    }

    private CommentaryEventResponse mapToResponse(MatchCommentaryEvent event, TeamPlayer tp) {
        CommentaryEventResponse.CommentaryEventResponseBuilder builder = CommentaryEventResponse.builder()
                .id(event.getId())
                .fixtureId(event.getFixture().getId())
                .eventType(event.getEventType())
                .eventTimeSeconds(event.getEventTimeSeconds())
                .quarter(event.getQuarter())
                .homeScore(event.getHomeScore())
                .awayScore(event.getAwayScore())
                .description(event.getDescription())
                .playerEventSubtype(event.getPlayerEventSubtype())
                .createdBy(event.getCreatedBy())
                .createdDate(event.getCreatedDate());

        if (tp != null) {
            builder.playerTeamPlayerId(tp.getId());
            String name = tp.getAthlete() != null ? tp.getAthlete().getEmail() : null;
            builder.playerName(name);
            builder.jerseyNumber(tp.getJerseyNumber());
        }

        return builder.build();
    }
}
