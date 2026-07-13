package com.feadev.eaids.positionprofile.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "position_scoring_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionScoringMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_profile_id", nullable = false)
    private PositionProfile positionProfile;

    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;

    @Column(name = "max_score", nullable = false)
    private int maxScore;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(length = 50)
    private String category;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
