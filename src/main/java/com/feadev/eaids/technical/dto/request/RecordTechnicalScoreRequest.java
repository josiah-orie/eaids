package com.feadev.eaids.technical.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordTechnicalScoreRequest {

    @NotNull(message = "Enrollment ID is required")
    private UUID enrollmentId;

    @NotNull(message = "Metric ID is required")
    private UUID metricId;

    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score must not exceed 100")
    private int score;
}
