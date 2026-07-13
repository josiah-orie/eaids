package com.feadev.eaids.positionprofile.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightTemplateRequest {

    @NotNull(message = "Physical weight is required")
    @DecimalMin(value = "0.00", message = "Physical weight must be at least 0")
    @DecimalMax(value = "100.00", message = "Physical weight must not exceed 100")
    private BigDecimal physicalWeight;

    @NotNull(message = "Technical weight is required")
    @DecimalMin(value = "0.00", message = "Technical weight must be at least 0")
    @DecimalMax(value = "100.00", message = "Technical weight must not exceed 100")
    private BigDecimal technicalWeight;

    @NotNull(message = "Tactical weight is required")
    @DecimalMin(value = "0.00", message = "Tactical weight must be at least 0")
    @DecimalMax(value = "100.00", message = "Tactical weight must not exceed 100")
    private BigDecimal tacticalWeight;

    @NotNull(message = "Attendance weight is required")
    @DecimalMin(value = "0.00", message = "Attendance weight must be at least 0")
    @DecimalMax(value = "100.00", message = "Attendance weight must not exceed 100")
    private BigDecimal attendanceWeight;

    @NotNull(message = "Discipline weight is required")
    @DecimalMin(value = "0.00", message = "Discipline weight must be at least 0")
    @DecimalMax(value = "100.00", message = "Discipline weight must not exceed 100")
    private BigDecimal disciplineWeight;

    @NotNull(message = "Coach review weight is required")
    @DecimalMin(value = "0.00", message = "Coach review weight must be at least 0")
    @DecimalMax(value = "100.00", message = "Coach review weight must not exceed 100")
    private BigDecimal coachReviewWeight;

    @NotNull(message = "Match stats weight is required")
    @DecimalMin(value = "0.00", message = "Match stats weight must be at least 0")
    @DecimalMax(value = "100.00", message = "Match stats weight must not exceed 100")
    private BigDecimal matchStatsWeight;
}
