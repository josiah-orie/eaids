package com.feadev.eaids.analytics.injuryrisk.dto;

import com.feadev.eaids.analytics.injuryrisk.model.enums.RiskLevel;
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
public class InjuryRiskResponse {

    // ---- Training-based fields (existing) ----
    private UUID id;
    private UUID athleteUserId;
    private UUID enrollmentId;
    /** Training-load composite risk score (0–100). */
    private int riskScore;
    /** Risk level derived from training load, attendance, and performance. */
    private RiskLevel riskLevel;
    private BigDecimal loadFactorScore;
    private BigDecimal attendanceFactorScore;
    private BigDecimal performanceFactorScore;
    private List<String> recommendations;
    private LocalDateTime assessedAt;

    // ---- Medical-history fields (new) ----
    /** Raw additive heuristic score from the medical risk engine (not normalised to 0-100). */
    private int medicalRiskScore;
    /** Risk level derived from medical history and clearance status. */
    private RiskLevel medicalRiskLevel;
    /** Explanatory flag codes from the medical risk engine. */
    private List<String> medicalFlags;
    /** Plain-English summary of the medical risk assessment. */
    private String medicalSummary;
    /** Combined overall risk level — the higher of training and medical risk,
     *  with clearance-status escalation applied. */
    private RiskLevel overallRiskLevel;
}
