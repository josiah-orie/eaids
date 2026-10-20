package com.feadev.eaids.league.repository;

import com.feadev.eaids.league.model.League;
import com.feadev.eaids.league.model.enums.LeagueStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeagueRepository extends JpaRepository<League, UUID> {
    List<League> findByStatus(LeagueStatus status);
    List<League> findByCountry(String country);
    List<League> findBySeasonYear(int seasonYear);
}
