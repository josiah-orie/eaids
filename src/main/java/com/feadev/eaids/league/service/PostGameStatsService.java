package com.feadev.eaids.league.service;

import com.feadev.eaids.league.dto.request.UpdatePostGameStatsRequest;
import com.feadev.eaids.league.dto.response.FixturePostGameResponse;
import com.feadev.eaids.league.dto.response.PostGameSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PostGameStatsService {

    /**
     * Aggregates all {@code LeagueStatEntry} records for the fixture and
     * upserts one {@code PostGameSummary} row per athlete.
     * <p>This method is executed asynchronously and does not return a value;
     * it is safe to call multiple times (idempotent via upsert).
     *
     * @param fixtureId the completed fixture to compute stats for
     */
    void computeAndStore(UUID fixtureId);

    /**
     * Returns per-athlete post-game summaries grouped by home/away team.
     */
    FixturePostGameResponse getSummaryByFixture(UUID fixtureId);

    /**
     * Returns a paginated per-fixture post-game history for a single athlete.
     */
    Page<PostGameSummaryResponse> getSummaryForAthlete(UUID athleteUserId, Pageable pageable);

    /**
     * Returns the post-game summary for a specific player in a specific fixture.
     */
    PostGameSummaryResponse getSummaryForPlayer(UUID fixtureId, UUID teamPlayerId);

    /**
     * Applies a partial field-level update to an existing {@link com.feadev.eaids.league.model.PostGameSummary}.
     *
     * <p>Only non-null fields in {@code request} are written; null fields are left as-is.
     * Throws {@link com.feadev.eaids.registrations.exception.InvalidStateTransitionException}
     * if the fixture's current status is not {@code IN_PROGRESS} or {@code COMPLETED}.
     *
     * @param fixtureId    the fixture the summary belongs to
     * @param teamPlayerId the player whose summary is being updated
     * @param request      partial update payload; null fields are ignored
     * @return the updated summary
     */
    PostGameSummaryResponse updatePostGameStats(UUID fixtureId, UUID teamPlayerId,
                                                UpdatePostGameStatsRequest request);
}
