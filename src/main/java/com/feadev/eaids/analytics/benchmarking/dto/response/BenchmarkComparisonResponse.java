package com.feadev.eaids.analytics.benchmarking.dto.response;

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
public class BenchmarkComparisonResponse {

    private UUID id;
    private UUID enrollmentId;
    private UUID benchmarkProfileId;
    private String benchmarkName;
    private BigDecimal physicalGap;
    private BigDecimal technicalGap;
    private BigDecimal tacticalGap;
    private BigDecimal overallGap;
    private BigDecimal percentileRank;
    private LocalDateTime comparedAt;
}
