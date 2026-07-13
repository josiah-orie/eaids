package com.feadev.eaids.analytics.prediction.dto;

import com.feadev.eaids.analytics.prediction.model.enums.PredictionTier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PotentialPredictionResponse {

    private UUID id;
    private UUID athleteUserId;
    private PredictionTier predictionTier;
    private BigDecimal overallGrowthRate;
    private BigDecimal physicalDelta;
    private BigDecimal technicalDelta;
    private BigDecimal tacticalDelta;
    private BigDecimal projectedScore1yr;
    private BigDecimal projectedScore2yr;
    private BigDecimal confidenceLevel;
    private LocalDateTime calculatedAt;
    private String message;
}
