package com.feadev.eaids.league.service;

import com.feadev.eaids.league.dto.request.AssignStaffRequest;
import com.feadev.eaids.league.dto.request.UpdateStaffRequest;
import com.feadev.eaids.league.dto.response.LeagueStaffResponse;

import java.util.List;
import java.util.UUID;

public interface LeagueStaffService {
    LeagueStaffResponse assignStaff(UUID leagueId, AssignStaffRequest request);
    List<LeagueStaffResponse> getStaffForLeague(UUID leagueId);
    LeagueStaffResponse updateStaffAssignment(UUID staffId, UpdateStaffRequest request);
    void removeStaff(UUID staffId);
}
