package com.feadev.eaids.positionprofile.dto.request;

import com.feadev.eaids.technical.model.enums.FootballPosition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePositionProfileRequest {

    @NotNull(message = "Position is required")
    private FootballPosition position;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotEmpty(message = "At least one scoring metric is required")
    @Valid
    private List<ScoringMetricRequest> metrics;

    @NotNull(message = "Weight template is required")
    @Valid
    private WeightTemplateRequest weightTemplate;
}
