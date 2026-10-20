package com.feadev.eaids.league.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueStatEntryResponse {

    private UUID id;
    private UUID teamPlayerId;
    private String playerName;
    private Integer jerseyNumber;
    private String position;
    private UUID fixtureId;
    private String statName;
    private String unit;
    private String positionGroup;
    private BigDecimal value;
    private LocalDateTime recordedDate;
}
