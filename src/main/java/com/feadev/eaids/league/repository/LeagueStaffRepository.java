package com.feadev.eaids.league.repository;

import com.feadev.eaids.league.model.LeagueStaff;
import com.feadev.eaids.league.model.enums.LeagueStaffRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeagueStaffRepository extends JpaRepository<LeagueStaff, UUID> {
    List<LeagueStaff> findByLeagueId(UUID leagueId);
    boolean existsByLeagueIdAndUserIdAndStaffRole(UUID leagueId, UUID userId, LeagueStaffRole staffRole);
    boolean existsByLeagueIdAndUserIdAndStaffRoleAndIdNot(UUID leagueId, UUID userId, LeagueStaffRole staffRole, UUID id);
}
