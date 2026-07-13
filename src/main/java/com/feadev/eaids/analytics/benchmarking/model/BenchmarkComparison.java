package com.feadev.eaids.analytics.benchmarking.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "benchmark_comparisons")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenchmarkComparison {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "enrollment_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID enrollmentId;

    @Column(name = "benchmark_profile_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID benchmarkProfileId;

    @Column(name = "physical_gap", precision = 5, scale = 2)
    private BigDecimal physicalGap;

    @Column(name = "technical_gap", precision = 5, scale = 2)
    private BigDecimal technicalGap;

    @Column(name = "tactical_gap", precision = 5, scale = 2)
    private BigDecimal tacticalGap;

    @Column(name = "overall_gap", precision = 5, scale = 2)
    private BigDecimal overallGap;

    @Column(name = "percentile_rank", precision = 5, scale = 2)
    private BigDecimal percentileRank;

    @Column(name = "compared_at", nullable = false)
    private LocalDateTime comparedAt;
}
