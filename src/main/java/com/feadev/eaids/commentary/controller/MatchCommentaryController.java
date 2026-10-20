package com.feadev.eaids.commentary.controller;

import com.feadev.eaids.commentary.SseEmitterRegistry;
import com.feadev.eaids.commentary.dto.request.PublishCommentaryRequest;
import com.feadev.eaids.commentary.dto.response.CommentaryEventResponse;
import com.feadev.eaids.commentary.service.MatchCommentaryService;
import com.feadev.eaids.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fixtures/{fixtureId}/commentary")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Match Commentary", description = "Live game commentary and score updates via SSE")
public class MatchCommentaryController {

    private final MatchCommentaryService commentaryService;
    private final SseEmitterRegistry sseRegistry;

    // -----------------------------------------------------------------------
    // Public SSE stream — unauthenticated fans can subscribe
    // -----------------------------------------------------------------------

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(
        summary = "Subscribe to live match commentary stream",
        description = "Open an SSE connection to receive real-time game events. " +
                      "Recent events (up to 50) are replayed immediately on connect " +
                      "so late subscribers see the current match state. " +
                      "This endpoint is publicly accessible — no authentication required."
    )
    @ApiResponse(responseCode = "200", description = "SSE stream opened")
    public SseEmitter streamCommentary(@PathVariable UUID fixtureId) {
        log.debug("New SSE subscriber for fixture {}", fixtureId);

        // Subscribe first — any events published between replay + subscribe won't be missed
        // because the broadcast loop goes through the same registry list.
        SseEmitter emitter = sseRegistry.subscribe(fixtureId);

        // Replay recent events so the new subscriber sees current match state
        List<CommentaryEventResponse> recent = commentaryService.getRecentEvents(fixtureId);
        for (CommentaryEventResponse event : recent) {
            sseRegistry.sendReplay(emitter, "replay", event);
        }

        return emitter;
    }

    // -----------------------------------------------------------------------
    // Public commentary history — unauthenticated read
    // -----------------------------------------------------------------------

    @GetMapping
    @Operation(
        summary = "Get commentary history for a fixture",
        description = "Returns the full or partial event log. " +
                      "Use the optional 'since' parameter (ISO-8601 datetime) for incremental polling."
    )
    @ApiResponse(responseCode = "200", description = "Commentary events returned")
    public ResponseEntity<List<CommentaryEventResponse>> getCommentary(
            @PathVariable UUID fixtureId,
            @RequestParam(required = false) LocalDateTime since) {

        List<CommentaryEventResponse> events = since != null
                ? commentaryService.getEventsSince(fixtureId, since)
                : commentaryService.getAllEvents(fixtureId);

        return ResponseEntity.ok(events);
    }

    // -----------------------------------------------------------------------
    // Protected publish — ADMIN or LEAGUE_COORDINATOR only
    // -----------------------------------------------------------------------

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or @leagueStaffAuthz.isCoordinatorForFixture(#fixtureId, authentication)")
    @Operation(
        summary = "Publish a game event to the live stream",
        description = "Publish a structured game event (score update, commentary, quarter marker, " +
                      "penalty, or player event). Fixture must be IN_PROGRESS. " +
                      "Requires ADMIN role or LEAGUE_COORDINATOR staff assignment for this fixture's league."
    )
    @ApiResponse(responseCode = "201", description = "Event published and broadcast")
    @ApiResponse(responseCode = "400", description = "Fixture is not IN_PROGRESS or invalid payload")
    @ApiResponse(responseCode = "403", description = "Insufficient permissions")
    public ResponseEntity<CommentaryEventResponse> publishEvent(
            @PathVariable UUID fixtureId,
            @Valid @RequestBody PublishCommentaryRequest request,
            Authentication authentication) {

        CustomUserDetails actor = (CustomUserDetails) authentication.getPrincipal();
        CommentaryEventResponse response = commentaryService.publishEvent(fixtureId, request, actor.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
