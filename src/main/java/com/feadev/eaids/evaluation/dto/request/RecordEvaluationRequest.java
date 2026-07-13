package com.feadev.eaids.evaluation.dto.request;

import jakarta.validation.constraints.DecimalMax;
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
public class RecordEvaluationRequest {

    @NotNull(message = "Enrollment ID is required")
    private UUID enrollmentId;

    @DecimalMin("0") @DecimalMax("100")
    private BigDecimal technicalScore;

    @DecimalMin("0") @DecimalMax("100")
    private BigDecimal tacticalScore;

    @DecimalMin("0") @DecimalMax("100")
    private BigDecimal leadershipScore;

    @DecimalMin("0") @DecimalMax("100")
    private BigDecimal characterScore;

    private String recommendation;
    private String comments;
}
