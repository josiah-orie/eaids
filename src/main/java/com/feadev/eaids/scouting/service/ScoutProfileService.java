package com.feadev.eaids.scouting.service;

import com.feadev.eaids.scouting.dto.request.CreateScoutProfileRequest;
import com.feadev.eaids.scouting.dto.request.GrantScopeRequest;
import com.feadev.eaids.scouting.dto.response.ScoutProfileResponse;
import com.feadev.eaids.scouting.dto.response.ScopeResponse;

import java.util.List;
import java.util.UUID;

public interface ScoutProfileService {

    ScoutProfileResponse createProfile(UUID userId, CreateScoutProfileRequest request);

    ScoutProfileResponse getProfile(UUID userId);

    ScopeResponse grantScope(UUID scoutUserId, GrantScopeRequest request);

    void revokeScope(UUID scopeId);

    List<ScopeResponse> getScopes(UUID scoutUserId);
}
