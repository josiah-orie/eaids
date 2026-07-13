package com.feadev.eaids.nationalranking.service;

import com.feadev.eaids.nationalranking.dto.request.CreateRankingConfigRequest;
import com.feadev.eaids.nationalranking.dto.response.NationalRankingResponse;
import com.feadev.eaids.nationalranking.dto.response.RankingConfigResponse;
import com.feadev.eaids.nationalranking.model.enums.RankingScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NationalRankingService {

    RankingConfigResponse createConfig(CreateRankingConfigRequest request);

    List<RankingConfigResponse> getAllConfigs();

    int generateRankings(UUID configId);

    Page<NationalRankingResponse> getTopRankings(RankingScope scope, String scopeValue, Pageable pageable);

    List<NationalRankingResponse> getAthleteRankings(UUID athleteUserId);
}
