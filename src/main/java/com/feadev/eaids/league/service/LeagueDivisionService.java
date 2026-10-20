package com.feadev.eaids.league.service;

import com.feadev.eaids.league.dto.request.CreateDivisionRequest;
import com.feadev.eaids.league.dto.request.UpdateDivisionRequest;
import com.feadev.eaids.league.dto.response.DivisionResponse;

import java.util.List;
import java.util.UUID;

public interface LeagueDivisionService {
    DivisionResponse createDivision(UUID leagueId, CreateDivisionRequest request);
    List<DivisionResponse> getDivisionsForLeague(UUID leagueId);
    DivisionResponse getDivision(UUID divisionId);
    DivisionResponse updateDivision(UUID divisionId, UpdateDivisionRequest request);
    void deleteDivision(UUID divisionId);
}
