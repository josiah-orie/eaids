package com.feadev.eaids.league.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostGameStatsRequest {

    @DecimalMin(value = "0.0", message = "totalPassingYards must be non-negative")
    private BigDecimal totalPassingYards;

    @DecimalMin(value = "0.0", message = "totalRushingYards must be non-negative")
    private BigDecimal totalRushingYards;

    @DecimalMin(value = "0.0", message = "totalReceivingYards must be non-negative")
    private BigDecimal totalReceivingYards;

    @DecimalMin(value = "0.0", message = "totalTackles must be non-negative")
    private BigDecimal totalTackles;

    @DecimalMin(value = "0.0", message = "touchdowns must be non-negative")
    private BigDecimal touchdowns;

    @DecimalMin(value = "0.0", message = "interceptions must be non-negative")
    private BigDecimal interceptions;

    @DecimalMin(value = "0.0", message = "sacks must be non-negative")
    private BigDecimal sacks;

    @DecimalMin(value = "0.0", message = "fumbles must be non-negative")
    private BigDecimal fumbles;

    @DecimalMin(value = "0.0", message = "penalties must be non-negative")
    private BigDecimal penalties;

    /**
     * Optional override for custom (non-standard) stats.
     * When provided, it REPLACES the entire customStats blob for this player/fixture.
     * Omit the field (null) to leave existing custom stats untouched.
     */
    private Map<String, BigDecimal> customStats;
}
