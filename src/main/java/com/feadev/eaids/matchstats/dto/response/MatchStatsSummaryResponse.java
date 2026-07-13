package com.feadev.eaids.matchstats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchStatsSummaryResponse {

    private UUID campId;
    private List<AthleteStatSummary> athletes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AthleteStatSummary {
        private UUID enrollmentId;
        private String athleteName;
        private int gamesPlayed;
        private List<StatAggregate> stats;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatAggregate {
        private String statName;
        private String unit;
        private BigDecimal total;
        private BigDecimal average;
    }
}
