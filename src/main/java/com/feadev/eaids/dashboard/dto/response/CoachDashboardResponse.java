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
public class CoachDashboardResponse {

    private long assignedAthletesCount;
    private EvaluationActivity evaluationActivity;
    private DevelopmentPlansSummary developmentPlansSummary;
    private List<CampActivityItem> campActivity;
    private List<RecentEvaluationItem> recentEvaluations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EvaluationActivity {
        private long totalEvaluationsSubmitted;
        private long thisMonth;
        private long lastMonth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DevelopmentPlansSummary {
        private long activePlans;
        private long completedPlans;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampActivityItem {
        private UUID campId;
        private String campName;
        private boolean isLead;
        private LocalDate startDate;
        private LocalDate endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentEvaluationItem {
        private String athleteName;
        private BigDecimal technicalScore;
        private BigDecimal tacticalScore;
        private LocalDateTime date;
    }
}
