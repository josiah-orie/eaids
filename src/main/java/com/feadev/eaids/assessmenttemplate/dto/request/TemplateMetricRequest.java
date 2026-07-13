package com.feadev.eaids.assessmenttemplate.dto.request;

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
public class TemplateMetricRequest {

    @NotBlank(message = "Metric name is required")
    @Size(max = 100, message = "Metric name must not exceed 100 characters")
    private String metricName;

    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @Size(max = 30, message = "Unit must not exceed 30 characters")
    private String unit;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.01", message = "Weight must be greater than 0")
    @DecimalMax(value = "100.00", message = "Weight must not exceed 100")
    private BigDecimal weight;

    @NotNull(message = "Max score is required")
    @Min(value = 1, message = "Max score must be at least 1")
    private Integer maxScore;

    @NotNull(message = "Display order is required")
    @Min(value = 1, message = "Display order must be at least 1")
    private Integer displayOrder;
}
