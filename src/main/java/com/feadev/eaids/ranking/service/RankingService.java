package com.feadev.eaids.ranking.service;

import com.feadev.eaids.ranking.dto.response.RankingResponse;
import com.feadev.eaids.ranking.model.enums.RankingType;

import java.util.List;
import java.util.UUID;

public interface RankingService {

    List<RankingResponse> generateRankings(UUID campId);

    List<RankingResponse> getRankingsByType(UUID campId, RankingType type);

    List<RankingResponse> getRankingsByTypeAndFilter(UUID campId, RankingType type, String filterValue);

    List<RankingResponse> getRankingsByEnrollment(UUID enrollmentId);
}
