package com.feadev.eaids.tactical.dto.request;

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
public class CreateTacticalMetricRequest {

    @NotBlank(message = "Metric name is required")
    @Size(max = 100, message = "Metric name must not exceed 100 characters")
    private String metricName;

    @Min(value = 1, message = "Max score must be at least 1")
    @Max(value = 100, message = "Max score must not exceed 100")
    private int maxScore;

    private BigDecimal weight;

    private String description;
}
