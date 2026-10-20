package com.feadev.eaids.league.service;

import com.feadev.eaids.league.dto.request.DraftPlayerRequest;
import com.feadev.eaids.league.dto.request.TransferPlayerRequest;
import com.feadev.eaids.league.dto.response.PlayerTransferResponse;
import com.feadev.eaids.league.dto.response.TeamPlayerResponse;

import java.util.List;
import java.util.UUID;

public interface PlayerDraftService {
    TeamPlayerResponse draftPlayer(UUID teamId, DraftPlayerRequest request);
    TeamPlayerResponse transferPlayer(UUID teamPlayerId, TransferPlayerRequest request);
    TeamPlayerResponse releasePlayer(UUID teamPlayerId, String notes);
    List<TeamPlayerResponse> getTeamRoster(UUID teamId);
    List<PlayerTransferResponse> getPlayerTransferHistory(UUID athleteUserId);
}
