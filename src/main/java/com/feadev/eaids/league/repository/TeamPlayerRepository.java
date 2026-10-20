package com.feadev.eaids.league.repository;

import com.feadev.eaids.league.model.TeamPlayer;
import com.feadev.eaids.league.model.enums.PlayerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamPlayerRepository extends JpaRepository<TeamPlayer, UUID> {
    List<TeamPlayer> findByTeamIdAndStatus(UUID teamId, PlayerStatus status);
    List<TeamPlayer> findByTeamId(UUID teamId);
    boolean existsByTeamIdAndAthleteId(UUID teamId, UUID athleteId);
    boolean existsByTeamIdAndJerseyNumber(UUID teamId, int jerseyNumber);
    boolean existsByTeamIdAndJerseyNumberAndIdNot(UUID teamId, int jerseyNumber, UUID id);
    Optional<TeamPlayer> findByAthleteIdAndStatus(UUID athleteId, PlayerStatus status);
    boolean existsByTeamIdAndStatus(UUID teamId, PlayerStatus status);

    /** Used by the trend service to load all players in a league's teams. */
    List<TeamPlayer> findByTeamLeagueId(UUID leagueId);
}
