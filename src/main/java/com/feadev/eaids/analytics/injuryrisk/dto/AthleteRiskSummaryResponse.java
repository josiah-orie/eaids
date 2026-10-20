package com.feadev.eaids.analytics.injuryrisk.dto;

import com.feadev.eaids.analytics.injuryrisk.model.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Athlete-safe subset of the holistic risk profile.
 *
 * <p>Returned when an athlete requests their own risk profile
 * ({@code GET /api/v1/analytics/injury-risk/athlete/{athleteUserId}/full}).
 * Raw factor scores and heuristic point totals are intentionally excluded to
 * prevent misinterpretation without clinical context.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AthleteRiskSummaryResponse {

    private UUID athleteUserId;
    /** Combined overall risk level — the highest-priority classification across all dimensions. */
    private RiskLevel overallRiskLevel;
    /** Risk level derived from medical history and clearance status. */
    private RiskLevel medicalRiskLevel;
    /** Explanatory flag codes from the medical risk engine. */
    private List<String> medicalFlags;
    /** Plain-English summary of the medical risk assessment. */
    private String medicalSummary;
    /** Recommendations from the most recent training assessment. */
    private List<String> recommendations;
    /** Timestamp of the most recent stored training assessment (or now if none exist). */
    private LocalDateTime assessedAt;
}
