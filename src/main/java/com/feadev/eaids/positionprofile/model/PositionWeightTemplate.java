package com.feadev.eaids.positionprofile.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "position_weight_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionWeightTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_profile_id", nullable = false, unique = true)
    private PositionProfile positionProfile;

    @Builder.Default
    @Column(name = "physical_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal physicalWeight = new BigDecimal("20.00");

    @Builder.Default
    @Column(name = "technical_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal technicalWeight = new BigDecimal("25.00");

    @Builder.Default
    @Column(name = "tactical_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal tacticalWeight = new BigDecimal("20.00");

    @Builder.Default
    @Column(name = "attendance_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal attendanceWeight = new BigDecimal("5.00");

    @Builder.Default
    @Column(name = "discipline_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal disciplineWeight = new BigDecimal("5.00");

    @Builder.Default
    @Column(name = "coach_review_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal coachReviewWeight = new BigDecimal("25.00");

    @Builder.Default
    @Column(name = "match_stats_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal matchStatsWeight = new BigDecimal("0.00");
}
