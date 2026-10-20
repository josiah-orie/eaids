package com.feadev.eaids.commentary.repository;

import com.feadev.eaids.commentary.model.MatchCommentaryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MatchCommentaryEventRepository extends JpaRepository<MatchCommentaryEvent, UUID> {

    /** Full chronological history for a fixture (used for commentary archive). */
    List<MatchCommentaryEvent> findByFixtureIdOrderByCreatedDateAsc(UUID fixtureId);

    /** Most recent 50 events in reverse order — reversed in service layer for replay. */
    List<MatchCommentaryEvent> findTop50ByFixtureIdOrderByCreatedDateDesc(UUID fixtureId);

    /** Events added after a given timestamp — for incremental polling fallback. */
    List<MatchCommentaryEvent> findByFixtureIdAndCreatedDateAfterOrderByCreatedDateAsc(
            UUID fixtureId, LocalDateTime since);
}
