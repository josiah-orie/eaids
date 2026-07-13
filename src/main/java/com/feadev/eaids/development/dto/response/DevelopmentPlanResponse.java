package com.feadev.eaids.development.dto.response;

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
public class DevelopmentPlanResponse {

    private UUID id;
    private UUID athleteUserId;
    private String athleteName;
    private UUID campId;
    private UUID coachUserId;
    private String status;
    private LocalDate startDate;
    private LocalDate targetEndDate;
    private String overallAssessment;
    private List<StrengthResponse> strengths;
    private List<WeaknessResponse> weaknesses;
    private List<GoalResponse> goals;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StrengthResponse {
        private UUID id;
        private String category;
        private String description;
        private BigDecimal score;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeaknessResponse {
        private UUID id;
        private String category;
        private String description;
        private BigDecimal score;
        private String targetScore;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalResponse {
        private UUID id;
        private String title;
        private String description;
        private String metric;
        private BigDecimal currentValue;
        private BigDecimal targetValue;
        private LocalDate targetDate;
        private String status;
    }
}
