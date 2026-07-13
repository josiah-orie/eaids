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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAssessmentTemplateRequest {

    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    private AgeCategory ageCategory;

    @DecimalMin(value = "0.00", message = "Pass threshold must be at least 0")
    @DecimalMax(value = "100.00", message = "Pass threshold must not exceed 100")
    private BigDecimal passThreshold;

    @Min(value = 1, message = "Scoring scale max must be at least 1")
    private Integer scoringScaleMax;

    @Valid
    private List<TemplateMetricRequest> metrics;
}
