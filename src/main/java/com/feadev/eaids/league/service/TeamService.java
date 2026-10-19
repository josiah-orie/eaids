package com.feadev.eaids.league.service;

import com.feadev.eaids.league.dto.request.AssignTeamCoachRequest;
import com.feadev.eaids.league.dto.request.CreateTeamRequest;
import com.feadev.eaids.league.dto.request.UpdateTeamRequest;
import com.feadev.eaids.league.dto.response.TeamCoachResponse;
import com.feadev.eaids.league.dto.response.TeamPlayerResponse;
import com.feadev.eaids.league.dto.response.TeamResponse;
import com.feadev.eaids.league.model.enums.TeamCoachRole;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface TeamService {
    TeamResponse createTeam(CreateTeamRequest request);
    TeamResponse getTeam(UUID teamId);
    List<TeamResponse> getAllTeams();
    List<TeamResponse> getTeamsByLeague(UUID leagueId);
    List<TeamResponse> getTeamsByDivision(UUID divisionId);
    TeamResponse updateTeam(UUID teamId, UpdateTeamRequest request);
    void deleteTeam(UUID teamId);
    TeamResponse uploadTeamLogo(UUID teamId, MultipartFile file);
    TeamResponse deleteTeamLogo(UUID teamId);

    List<TeamPlayerResponse> getTeamPlayers(UUID leagueId, UUID teamId);

    // Coach assignment
    TeamCoachResponse assignCoach(UUID teamId, AssignTeamCoachRequest request);
    TeamCoachResponse updateCoach(UUID teamId, TeamCoachRole role, AssignTeamCoachRequest request);
    List<TeamCoachResponse> getCoaches(UUID teamId);
    void removeCoach(UUID teamId, TeamCoachRole role);
}
