package com.feadev.eaids.league.service;

import com.feadev.eaids.league.dto.request.CreateLeagueRequest;
import com.feadev.eaids.league.dto.request.UpdateLeagueRequest;
import com.feadev.eaids.league.dto.response.LeagueResponse;
import com.feadev.eaids.league.model.enums.LeagueStatus;

import java.util.List;
import java.util.UUID;

public interface LeagueService {
    LeagueResponse createLeague(CreateLeagueRequest request);
    LeagueResponse getLeague(UUID leagueId);
    List<LeagueResponse> getAllLeagues();
    List<LeagueResponse> getLeaguesByStatus(LeagueStatus status);
    List<LeagueResponse> getLeaguesByCountry(String country);
    LeagueResponse updateLeague(UUID leagueId, UpdateLeagueRequest request);
    void deleteLeague(UUID leagueId);
    LeagueResponse transitionStatus(UUID leagueId, LeagueStatus newStatus);
}
