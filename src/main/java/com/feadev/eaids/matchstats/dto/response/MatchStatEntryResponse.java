package com.feadev.eaids.matchstats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchStatEntryResponse {

    private UUID id;
    private UUID enrollmentId;
    private UUID campId;
    private UUID definitionId;
    private String statName;
    private String positionGroup;
    private BigDecimal value;
    private String unit;
    private LocalDate matchDate;
    private String opponent;
    private LocalDateTime createdDate;
}
