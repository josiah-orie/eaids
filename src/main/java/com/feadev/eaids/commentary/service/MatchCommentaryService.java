package com.feadev.eaids.commentary.service;

import com.feadev.eaids.commentary.dto.request.PublishCommentaryRequest;
import com.feadev.eaids.commentary.dto.response.CommentaryEventResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MatchCommentaryService {

    /**
     * Persists the event, optionally updates fixture scores (SCORE_UPDATE),
     * and broadcasts to all active SSE subscribers.
     *
     * @param fixtureId the target fixture — must be IN_PROGRESS
     * @param request   the event payload
     * @param actorId   the authenticated user publishing the event
     * @return the persisted event response
     * @throws IllegalStateException if the fixture is not IN_PROGRESS
     */
    CommentaryEventResponse publishEvent(UUID fixtureId, PublishCommentaryRequest request, UUID actorId);

    /**
     * Returns the most recent 50 events for a fixture in chronological order.
     * Used to replay history to new SSE subscribers.
     */
    List<CommentaryEventResponse> getRecentEvents(UUID fixtureId);

    /** Returns all events for a fixture after the given timestamp. */
    List<CommentaryEventResponse> getEventsSince(UUID fixtureId, LocalDateTime since);

    /** Returns the full chronological history of events for a fixture. */
    List<CommentaryEventResponse> getAllEvents(UUID fixtureId);
}
