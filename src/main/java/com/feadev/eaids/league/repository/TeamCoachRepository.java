package com.feadev.eaids.league.repository;

import com.feadev.eaids.league.model.TeamCoach;
import com.feadev.eaids.league.model.enums.TeamCoachRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamCoachRepository extends JpaRepository<TeamCoach, UUID> {

    List<TeamCoach> findByTeamId(UUID teamId);

    Optional<TeamCoach> findByTeamIdAndRole(UUID teamId, TeamCoachRole role);

    boolean existsByTeamIdAndRole(UUID teamId, TeamCoachRole role);

    boolean existsByTeamIdAndCoachIdAndRole(UUID teamId, UUID coachId, TeamCoachRole role);
}
