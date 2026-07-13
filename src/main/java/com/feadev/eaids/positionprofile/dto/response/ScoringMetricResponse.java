package com.feadev.eaids.positionprofile.dto.response;

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
public class ScoringMetricResponse {

    private UUID id;
    private String metricName;
    private int maxScore;
    private BigDecimal weight;
    private String category;
    private String description;
    private int displayOrder;
}
