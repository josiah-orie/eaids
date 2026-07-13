package com.feadev.eaids.history.service;

import com.feadev.eaids.history.dto.response.AthleteHistoryResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AthleteHistoryService {

    AthleteHistoryResponse getHistory(UUID athleteId, UUID campId, List<String> types,
                                       LocalDate fromDate, LocalDate toDate, int page, int size);
}
