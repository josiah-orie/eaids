package com.feadev.eaids.physical.model;

import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.registrations.model.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "metric_benchmarks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricBenchmark {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metric_id", nullable = false)
    private AssessmentMetric metric;

    @Column(name = "min_value", precision = 10, scale = 3)
    private BigDecimal minValue;

    @Column(name = "max_value", precision = 10, scale = 3)
    private BigDecimal maxValue;

    @Column(nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_category")
    private AgeCategory ageCategory;

    @Enumerated(EnumType.STRING)
    @Column
    private Gender gender;
}
