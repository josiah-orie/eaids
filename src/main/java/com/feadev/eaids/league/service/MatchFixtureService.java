package com.feadev.eaids.league.service;

import com.feadev.eaids.league.dto.request.CreateFixtureRequest;
import com.feadev.eaids.league.dto.request.TransitionFixtureRequest;
import com.feadev.eaids.league.dto.request.UpdateFixtureRequest;
import com.feadev.eaids.league.dto.response.MatchFixtureResponse;
import com.feadev.eaids.league.model.enums.FixtureStatus;
import com.feadev.eaids.matchstats.model.MatchStatEntry;

import java.util.List;
import java.util.UUID;

public interface MatchFixtureService {
    MatchFixtureResponse createFixture(UUID leagueId, CreateFixtureRequest request);
    MatchFixtureResponse getFixture(UUID fixtureId);
    List<MatchFixtureResponse> getFixturesByLeague(UUID leagueId, FixtureStatus status, UUID divisionId);
    List<MatchFixtureResponse> getFixturesByTeam(UUID teamId);
    MatchFixtureResponse updateFixture(UUID fixtureId, UpdateFixtureRequest request);
    MatchFixtureResponse transitionStatus(UUID fixtureId, TransitionFixtureRequest request);
    void deleteFixture(UUID fixtureId);
    List<MatchStatEntry> getFixtureStats(UUID fixtureId);
}
