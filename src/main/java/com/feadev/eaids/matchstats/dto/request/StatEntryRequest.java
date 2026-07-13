package com.feadev.eaids.matchstats.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatEntryRequest {

    @NotNull(message = "Definition ID is required")
    private UUID definitionId;

    @NotNull(message = "Value is required")
    @DecimalMin(value = "0.00", message = "Value must not be negative")
    private BigDecimal value;
}
