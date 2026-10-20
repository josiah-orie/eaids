package com.feadev.eaids.medical.dto.response;

import com.feadev.eaids.medical.risk.RiskLevel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Per-athlete injury risk assessment response.
 * Safe to return to MEDICAL_OFFICER, ADMIN, PHYSIOTHERAPIST, and the athlete themselves.
 */
@Data
@Builder
public class InjuryRiskResponse {

    private UUID athleteUserId;
    private String athleteName;

    private RiskLevel riskLevel;

    /**
     * Explanatory flag codes, e.g.:
     * <ul>
     *   <li>{@code HIGH_INJURY_FREQUENCY}</li>
     *   <li>{@code CONCUSSION_HISTORY}</li>
     *   <li>{@code RECURRENT_BODY_REGION:KNEE}</li>
     * </ul>
     */
    private List<String> flags;

    /** Human-readable one-sentence summary. */
    private String summary;

    /** When this score was last computed (relevant because result is cached). */
    private LocalDateTime computedAt;
}
