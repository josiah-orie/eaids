package com.feadev.eaids.tactical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TacticalMetricResponse {

    private UUID id;
    private UUID campId;
    private String metricName;
    private int maxScore;
    private BigDecimal weight;
    private String description;
    private LocalDateTime createdDate;
}
