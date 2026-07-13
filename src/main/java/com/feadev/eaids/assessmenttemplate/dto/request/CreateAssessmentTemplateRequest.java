package com.feadev.eaids.assessmenttemplate.dto.request;

import com.feadev.eaids.camp.model.enums.AgeCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAssessmentTemplateRequest {

    @NotNull(message = "Position profile ID is required")
    private UUID positionProfileId;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotNull(message = "Age category is required")
    private AgeCategory ageCategory;

    @NotNull(message = "Pass threshold is required")
    @DecimalMin(value = "0.00", message = "Pass threshold must be at least 0")
    @DecimalMax(value = "100.00", message = "Pass threshold must not exceed 100")
    private BigDecimal passThreshold;

    @NotNull(message = "Scoring scale max is required")
    @Min(value = 1, message = "Scoring scale max must be at least 1")
    private Integer scoringScaleMax;

    @NotEmpty(message = "At least one metric is required")
    @Valid
    private List<TemplateMetricRequest> metrics;
}
