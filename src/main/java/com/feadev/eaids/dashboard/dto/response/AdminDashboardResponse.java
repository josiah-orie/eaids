package com.feadev.eaids.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {

    private UserStats userStats;
    private CampStats campStats;
    private EvaluationStats evaluationStats;
    private List<TierDistributionItem> selectionTierDistribution;
    private ScoringOverview scoringOverview;
    private DevelopmentStats developmentStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStats {
        private long totalAthletes;
        private long totalCoaches;
        private long totalAdmins;
        private long activeAthletes;
        private long pendingReview;
        private long suspendedAccounts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampStats {
        private long totalCamps;
        private long activeCamps;
        private long completedCamps;
        private long totalEnrollments;
        private double averageEnrollmentPerCamp;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluationStats {
        private long totalEvaluations;
        private long evaluationsThisPeriod;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TierDistributionItem {
        private String tier;
        private long count;
        private double percentage;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ScoringOverview {
        private BigDecimal averageOverallScore;
        private BigDecimal highestScore;
        private BigDecimal lowestScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DevelopmentStats {
        private long totalPlans;
        private long activePlans;
        private long completedPlans;
    }
}
