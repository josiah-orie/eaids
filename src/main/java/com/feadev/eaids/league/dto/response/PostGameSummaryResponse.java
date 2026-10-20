package com.feadev.eaids.league.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Per-athlete statistics summary for a completed fixture.
 */
@Data
@Builder
public class PostGameSummaryResponse {

    private UUID id;
    private UUID fixtureId;
    private UUID teamPlayerId;
    private UUID athleteUserId;
    private UUID teamId;

    // Player identity (denormalised from TeamPlayer / User)
    private String playerName;
    private Integer jerseyNumber;
    private String position;

    // Typed stat columns
    private BigDecimal totalPassingYards;
    private BigDecimal totalRushingYards;
    private BigDecimal totalReceivingYards;
    private BigDecimal totalTackles;
    private BigDecimal touchdowns;
    private BigDecimal interceptions;
    private BigDecimal sacks;
    private BigDecimal fumbles;
    private BigDecimal penalties;

    /** Overflow stats not covered by the typed columns above. */
    private Map<String, BigDecimal> customStats;

    private LocalDateTime computedAt;
    private LocalDateTime createdDate;
}
