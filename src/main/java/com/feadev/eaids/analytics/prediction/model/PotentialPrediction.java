package com.feadev.eaids.analytics.prediction.model;

import com.feadev.eaids.analytics.prediction.model.enums.PredictionTier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "potential_predictions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PotentialPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "athlete_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID athleteUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "prediction_tier", nullable = false)
    private PredictionTier predictionTier;

    @Column(name = "overall_growth_rate", precision = 5, scale = 2)
    private BigDecimal overallGrowthRate;

    @Column(name = "physical_delta", precision = 5, scale = 2)
    private BigDecimal physicalDelta;

    @Column(name = "technical_delta", precision = 5, scale = 2)
    private BigDecimal technicalDelta;

    @Column(name = "tactical_delta", precision = 5, scale = 2)
    private BigDecimal tacticalDelta;

    @Column(name = "projected_score_1yr", precision = 5, scale = 2)
    private BigDecimal projectedScore1yr;

    @Column(name = "projected_score_2yr", precision = 5, scale = 2)
    private BigDecimal projectedScore2yr;

    @Column(name = "confidence_level", precision = 5, scale = 2)
    private BigDecimal confidenceLevel;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
}
