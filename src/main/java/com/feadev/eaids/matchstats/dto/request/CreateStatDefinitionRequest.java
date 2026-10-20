package com.feadev.eaids.matchstats.dto.request;

import com.feadev.eaids.matchstats.model.enums.PositionGroup;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStatDefinitionRequest {

    @NotNull(message = "positionGroup is required")
    private PositionGroup positionGroup;

    @NotBlank(message = "statName is required")
    @Size(max = 100, message = "statName must not exceed 100 characters")
    private String statName;

    @Size(max = 30, message = "unit must not exceed 30 characters")
    private String unit;

    @Min(value = 0, message = "displayOrder must be 0 or greater")
    private int displayOrder;
}
