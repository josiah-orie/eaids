package com.feadev.eaids.assessmenttemplate.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentTemplateResponse {

    private UUID id;
    private UUID positionProfileId;
    private String positionProfileName;
    private String position;
    private String name;
    private String ageCategory;
    private BigDecimal passThreshold;
    private int scoringScaleMax;
    private boolean active;
    private List<TemplateMetricResponse> metrics;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
