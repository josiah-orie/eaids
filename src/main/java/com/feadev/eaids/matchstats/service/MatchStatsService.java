package com.feadev.eaids.matchstats.service;

import com.feadev.eaids.matchstats.dto.request.CreateStatDefinitionRequest;
import com.feadev.eaids.matchstats.dto.request.RecordMatchStatsRequest;
import com.feadev.eaids.matchstats.dto.response.MatchStatDefinitionResponse;
import com.feadev.eaids.matchstats.dto.response.MatchStatEntryResponse;
import com.feadev.eaids.matchstats.dto.response.MatchStatsSummaryResponse;
import com.feadev.eaids.matchstats.model.enums.PositionGroup;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MatchStatsService {

    List<MatchStatDefinitionResponse> getDefinitions(PositionGroup positionGroup);

    MatchStatDefinitionResponse createDefinition(CreateStatDefinitionRequest request);

    List<MatchStatEntryResponse> recordStats(UUID campId, RecordMatchStatsRequest request, UUID recordedBy);

    List<MatchStatEntryResponse> getEnrollmentStats(UUID campId, UUID enrollmentId, LocalDate matchDate);

    MatchStatsSummaryResponse getCampSummary(UUID campId);
}
