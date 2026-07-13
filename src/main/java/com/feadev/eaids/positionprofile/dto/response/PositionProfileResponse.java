package com.feadev.eaids.positionprofile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionProfileResponse {

    private UUID id;
    private String position;
    private String name;
    private String description;
    private boolean active;
    private List<ScoringMetricResponse> metrics;
    private WeightTemplateResponse weightTemplate;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
