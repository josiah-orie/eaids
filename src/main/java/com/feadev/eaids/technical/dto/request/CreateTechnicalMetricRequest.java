package com.feadev.eaids.technical.dto.request;

import com.feadev.eaids.technical.model.enums.FootballPosition;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTechnicalMetricRequest {

    @NotNull(message = "Position is required")
    private FootballPosition position;

    @NotBlank(message = "Metric name is required")
    @Size(max = 100, message = "Metric name must not exceed 100 characters")
    private String metricName;

    @Min(value = 1, message = "Max score must be at least 1")
    @Max(value = 100, message = "Max score must not exceed 100")
    private int maxScore;

    private BigDecimal weight;
}
