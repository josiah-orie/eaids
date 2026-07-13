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

    private UUID id;
    private UUID athleteUserId;
    private UUID enrollmentId;
    private int riskScore;
    private RiskLevel riskLevel;
    private BigDecimal loadFactorScore;
    private BigDecimal attendanceFactorScore;
    private BigDecimal performanceFactorScore;
    private List<String> recommendations;
    private LocalDateTime assessedAt;
}
