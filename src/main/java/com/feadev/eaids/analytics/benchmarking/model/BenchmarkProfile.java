package com.feadev.eaids.analytics.benchmarking.model;

import com.feadev.eaids.analytics.benchmarking.model.enums.BenchmarkType;
import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.registrations.model.enums.Gender;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "benchmark_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BenchmarkProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "benchmark_type", nullable = false)
    private BenchmarkType benchmarkType;

    @Enumerated(EnumType.STRING)
    @Column
    private FootballPosition position;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_category")
    private AgeCategory ageCategory;

    @Enumerated(EnumType.STRING)
    @Column
    private Gender gender;

    @Column(name = "physical_benchmark", precision = 5, scale = 2)
    private BigDecimal physicalBenchmark;

    @Column(name = "technical_benchmark", precision = 5, scale = 2)
    private BigDecimal technicalBenchmark;

    @Column(name = "tactical_benchmark", precision = 5, scale = 2)
    private BigDecimal tacticalBenchmark;

    @Column(name = "overall_benchmark", precision = 5, scale = 2)
    private BigDecimal overallBenchmark;

    @Column(name = "source_description", columnDefinition = "TEXT")
    private String sourceDescription;

    @Builder.Default
    @Column(name = "auto_captured", nullable = false)
    private boolean autoCaptured = false;

    @Column(name = "captured_from_enrollment_id", columnDefinition = "BINARY(16)")
    private UUID capturedFromEnrollmentId;

    @Column(name = "created_by", columnDefinition = "BINARY(16)")
    private UUID createdBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;
}
