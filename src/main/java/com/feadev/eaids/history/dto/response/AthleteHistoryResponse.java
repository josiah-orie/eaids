package com.feadev.eaids.history.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AthleteHistoryResponse {

    private UUID athleteId;
    private String athleteName;
    private int totalCamps;
    private int totalEntries;
    private int page;
    private int size;
    private List<HistoryTimelineEntry> timeline;
}
