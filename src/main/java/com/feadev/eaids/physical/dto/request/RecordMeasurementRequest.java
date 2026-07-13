package com.feadev.eaids.physical.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordMeasurementRequest {

    @NotNull(message = "Enrollment ID is required")
    private UUID enrollmentId;

    @NotNull(message = "Metric ID is required")
    private UUID metricId;

    @NotNull(message = "Raw value is required")
    private BigDecimal rawValue;

    @NotNull(message = "Measured date is required")
    private LocalDate measuredDate;
}
