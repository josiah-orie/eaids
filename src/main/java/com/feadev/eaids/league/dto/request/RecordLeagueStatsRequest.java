package com.feadev.eaids.league.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordLeagueStatsRequest {

    @NotEmpty(message = "At least one stat entry is required")
    private List<@Valid PlayerStatEntry> entries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlayerStatEntry {

        @NotNull(message = "teamPlayerId is required")
        private UUID teamPlayerId;

        @NotNull(message = "definitionId is required")
        private UUID definitionId;

        @NotNull(message = "value is required")
        @DecimalMin(value = "0.0", message = "Stat value must be zero or greater")
        private BigDecimal value;
    }
}
