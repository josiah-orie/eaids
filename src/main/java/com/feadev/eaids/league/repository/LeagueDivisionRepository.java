package com.feadev.eaids.league.repository;

import com.feadev.eaids.league.model.LeagueDivision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeagueDivisionRepository extends JpaRepository<LeagueDivision, UUID> {
    List<LeagueDivision> findByLeagueIdOrderByTierRank(UUID leagueId);
    boolean existsByLeagueIdAndName(UUID leagueId, String name);
    boolean existsByLeagueIdAndNameAndIdNot(UUID leagueId, String name, UUID id);
}
