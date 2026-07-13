package com.feadev.eaids.physical.dto.request;

import com.feadev.eaids.physical.model.enums.MeasurementUnit;
import com.feadev.eaids.physical.model.enums.MetricCategory;
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
public class CreateMetricRequest {

    @NotNull(message = "Metric category is required")
    private MetricCategory category;

    @NotBlank(message = "Metric name is required")
    @Size(max = 100, message = "Metric name must not exceed 100 characters")
    private String metricName;

    @NotNull(message = "Measurement unit is required")
    private MeasurementUnit unit;

    private String description;

    @Builder.Default
    private boolean lowerIsBetter = false;
}
