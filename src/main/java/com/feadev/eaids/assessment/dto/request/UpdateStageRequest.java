package com.feadev.eaids.assessment.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStageRequest {

    @Size(max = 100, message = "Stage name must not exceed 100 characters")
    private String stageName;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private Integer durationDays;

    private BigDecimal scoreWeight;

    private Boolean isActive;
}
