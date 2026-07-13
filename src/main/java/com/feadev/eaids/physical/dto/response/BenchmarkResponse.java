package com.feadev.eaids.physical.dto.response;

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
public class BenchmarkResponse {

    private UUID id;
    private UUID metricId;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private int score;
    private String ageCategory;
    private String gender;
}
