package com.feadev.eaids.league.repository;

import com.feadev.eaids.league.model.LeagueStatEntry;
import com.feadev.eaids.league.model.enums.FixtureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeagueStatEntryRepository extends JpaRepository<LeagueStatEntry, UUID> {

    List<LeagueStatEntry> findByFixtureId(UUID fixtureId);

    List<LeagueStatEntry> findByTeamPlayerIdIn(List<UUID> teamPlayerIds);

    @Query("SELECT e FROM LeagueStatEntry e " +
           "WHERE e.fixture.id = :fixtureId " +
           "AND e.teamPlayer.id IN :teamPlayerIds")
    List<LeagueStatEntry> findByFixtureIdAndTeamPlayerIdIn(
            @Param("fixtureId") UUID fixtureId,
            @Param("teamPlayerIds") List<UUID> teamPlayerIds);

    @Query("SELECT e FROM LeagueStatEntry e " +
           "WHERE e.fixture.league.id = :leagueId " +
           "AND e.fixture.status = :status " +
           "AND e.teamPlayer.id IN :teamPlayerIds")
    List<LeagueStatEntry> findByLeagueIdAndFixtureStatusAndTeamPlayerIdIn(
            @Param("leagueId") UUID leagueId,
            @Param("status") FixtureStatus status,
            @Param("teamPlayerIds") List<UUID> teamPlayerIds);
}
