package com.feadev.eaids.analytics.recommendation.dto;

import com.feadev.eaids.technical.model.enums.FootballPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionRecommendationResponse {

    private UUID enrollmentId;
    private UUID athleteUserId;
    private String athleteName;

    private FootballPosition bestPosition;
    private BigDecimal bestPositionFitScore;

    private FootballPosition secondaryPosition;
    private BigDecimal secondaryFitScore;

    private FootballPosition potentialPosition;
    private BigDecimal potentialFitScore;

    private List<PositionFitScore> allPositionScores;
    private LocalDateTime generatedAt;
}
