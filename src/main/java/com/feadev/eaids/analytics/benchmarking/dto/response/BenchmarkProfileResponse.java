package com.feadev.eaids.analytics.benchmarking.dto.response;

import com.feadev.eaids.analytics.benchmarking.model.enums.BenchmarkType;
import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.registrations.model.enums.Gender;
import com.feadev.eaids.technical.model.enums.FootballPosition;
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
public class BenchmarkProfileResponse {

    private UUID id;
    private String name;
    private BenchmarkType benchmarkType;
    private FootballPosition position;
    private AgeCategory ageCategory;
    private Gender gender;
    private BigDecimal physicalBenchmark;
    private BigDecimal technicalBenchmark;
    private BigDecimal tacticalBenchmark;
    private BigDecimal overallBenchmark;
    private String sourceDescription;
    private boolean autoCaptured;
    private LocalDateTime createdDate;
}
