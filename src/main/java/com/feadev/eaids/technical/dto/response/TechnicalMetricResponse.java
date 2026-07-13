package com.feadev.eaids.technical.dto.response;

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
public class TechnicalMetricResponse {

    private UUID id;
    private UUID campId;
    private String position;
    private String metricName;
    private int maxScore;
    private BigDecimal weight;
    private LocalDateTime createdDate;
}
