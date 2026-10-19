package com.feadev.eaids.league.event;

import java.util.UUID;

/**
 * Application event published when a {@link com.feadev.eaids.league.model.MatchFixture}
 * transitions to {@code COMPLETED} status.
 *
 * <p>Consumed by {@code PostGameStatsServiceImpl#onFixtureCompleted} via a
 * {@code @TransactionalEventListener} so that post-game stat computation only
 * starts after the fixture transition has fully committed to the database.
 *
 * @param fixtureId the ID of the completed fixture
 * @param leagueId  the ID of the league the fixture belongs to
 */
public record FixtureCompletedEvent(UUID fixtureId, UUID leagueId) {
}
