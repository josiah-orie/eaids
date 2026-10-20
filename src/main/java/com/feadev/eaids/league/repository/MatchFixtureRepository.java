package com.feadev.eaids.league.repository;

import com.feadev.eaids.league.model.MatchFixture;
import com.feadev.eaids.league.model.enums.FixtureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MatchFixtureRepository extends JpaRepository<MatchFixture, UUID> {
    List<MatchFixture> findByLeagueId(UUID leagueId);
    List<MatchFixture> findByLeagueIdAndStatus(UUID leagueId, FixtureStatus status);
    List<MatchFixture> findByLeagueIdAndDivisionId(UUID leagueId, UUID divisionId);
    List<MatchFixture> findByDivisionId(UUID divisionId);

    @Query("SELECT f FROM MatchFixture f WHERE f.homeTeam.id = :teamId OR f.awayTeam.id = :teamId")
    List<MatchFixture> findByTeamId(@Param("teamId") UUID teamId);

    List<MatchFixture> findByScheduledDateBetween(LocalDateTime from, LocalDateTime to);

    // Standings queries — scope completed fixtures by division tag
    List<MatchFixture> findByLeagueIdAndDivisionIdAndStatus(
            UUID leagueId, UUID divisionId, FixtureStatus status);

    @Query("SELECT f FROM MatchFixture f " +
           "WHERE f.league.id = :leagueId " +
           "AND f.status = :status " +
           "AND f.division IS NULL")
    List<MatchFixture> findByLeagueIdAndStatusAndDivisionIsNull(
            @Param("leagueId") UUID leagueId,
            @Param("status") FixtureStatus status);
}
