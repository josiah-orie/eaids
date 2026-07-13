package com.feadev.eaids.analytics.injuryrisk.model;

import com.feadev.eaids.analytics.injuryrisk.model.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "injury_risk_assessments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InjuryRiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "athlete_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID athleteUserId;

    @Column(name = "enrollment_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID enrollmentId;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false)
    private RiskLevel riskLevel;

    @Column(name = "load_factor_score", precision = 5, scale = 2)
    private BigDecimal loadFactorScore;

    @Column(name = "attendance_factor_score", precision = 5, scale = 2)
    private BigDecimal attendanceFactorScore;

    @Column(name = "performance_factor_score", precision = 5, scale = 2)
    private BigDecimal performanceFactorScore;

    @Column(columnDefinition = "JSON")
    private String recommendations;

    @Column(name = "assessed_at", nullable = false)
    private LocalDateTime assessedAt;
}
