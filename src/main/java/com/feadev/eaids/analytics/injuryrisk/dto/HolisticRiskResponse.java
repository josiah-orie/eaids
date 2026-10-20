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

/**
 * Full holistic risk profile for an athlete, combining training-load and medical-history
 * risk dimensions into a single response.
 *
 * <p>Returned to {@code ADMIN} and {@code COACH} roles.
 * Athletes receive the filtered {@link AthleteRiskSummaryResponse} instead.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HolisticRiskResponse {

    private UUID athleteUserId;

    // ---- Training-based dimensions ----
    /** Training-load composite risk score (0–100). */
    private int trainingRiskScore;
    /** Risk level derived from training load, attendance, and performance. */
    private RiskLevel trainingRiskLevel;
    private BigDecimal loadFactorScore;
    private BigDecimal attendanceFactorScore;
    private BigDecimal performanceFactorScore;

    // ---- Medical-history dimensions ----
    /** Raw additive heuristic score from the medical risk engine (not normalised). */
    private int medicalRiskScore;
    /** Risk level derived from medical history and clearance status. */
    private RiskLevel medicalRiskLevel;
    /** Explanatory flag codes from the medical risk engine. */
    private List<String> medicalFlags;
    /** Plain-English summary of the medical risk assessment. */
    private String medicalSummary;

    // ---- Combined ----
    /** Overall risk level — higher of training/medical with clearance-status escalation. */
    private RiskLevel overallRiskLevel;
    /** Recommendations from the most recent training assessment. */
    private List<String> recommendations;
    /** Timestamp of the most recent stored training assessment (or now if none exist). */
    private LocalDateTime assessedAt;
}
