package com.feadev.eaids.physical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeasurementResponse {

    private UUID id;
    private UUID enrollmentId;
    private UUID metricId;
    private String metricName;
    private String category;
    private BigDecimal rawValue;
    private Integer calculatedScore;
    private LocalDate measuredDate;
    private String recordedBy;
    private LocalDateTime createdDate;
}
