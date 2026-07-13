package com.feadev.eaids.physical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricResponse {

    private UUID id;
    private UUID campId;
    private String category;
    private String metricName;
    private String unit;
    private String description;
    private boolean isActive;
    private boolean lowerIsBetter;
    private LocalDateTime createdDate;
}
