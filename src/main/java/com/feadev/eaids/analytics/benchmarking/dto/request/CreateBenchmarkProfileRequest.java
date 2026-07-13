package com.feadev.eaids.analytics.benchmarking.dto.request;

import com.feadev.eaids.analytics.benchmarking.model.enums.BenchmarkType;
import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.registrations.model.enums.Gender;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBenchmarkProfileRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Benchmark type is required")
    private BenchmarkType benchmarkType;

    private FootballPosition position;
    private AgeCategory ageCategory;
    private Gender gender;
    private BigDecimal physicalBenchmark;
    private BigDecimal technicalBenchmark;
    private BigDecimal tacticalBenchmark;
    private BigDecimal overallBenchmark;
    private String sourceDescription;
}
