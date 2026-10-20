package com.feadev.eaids.league.repository;

import com.feadev.eaids.league.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> {
    List<Team> findByLeagueId(UUID leagueId);
    List<Team> findByDivisionId(UUID divisionId);
    boolean existsByNameAndLeagueId(String name, UUID leagueId);
    boolean existsByNameAndLeagueIdAndIdNot(String name, UUID leagueId, UUID id);
    boolean existsByDivisionId(UUID divisionId);
}
