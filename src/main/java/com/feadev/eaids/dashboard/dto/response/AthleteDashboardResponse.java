package com.feadev.eaids.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AthleteDashboardResponse {

    private List<PerformanceTrendItem> performanceTrend;
    private List<RankingItem> latestRankings;
    private DevelopmentPlanSummary developmentPlanSummary;
    private List<UpcomingCampItem> upcomingCamps;
    private List<RecentEvaluationItem> recentEvaluations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceTrendItem {
        private String campName;
        private LocalDate date;
        private BigDecimal totalScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RankingItem {
        private String rankingType;
        private int rankValue;
        private int totalInCategory;
        private BigDecimal percentile;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DevelopmentPlanSummary {
        private long activePlans;
        private long goalsTotal;
        private long goalsAchieved;
        private double progressPercentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingCampItem {
        private UUID campId;
        private String name;
        private LocalDate startDate;
        private String enrollmentStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentEvaluationItem {
        private String evaluatorName;
        private BigDecimal technicalScore;
        private BigDecimal tacticalScore;
        private BigDecimal leadershipScore;
        private BigDecimal characterScore;
        private LocalDateTime date;
    }
}
