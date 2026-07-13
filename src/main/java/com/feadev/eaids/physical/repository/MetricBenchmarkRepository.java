package com.feadev.eaids.physical.repository;

import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.physical.model.MetricBenchmark;
import com.feadev.eaids.registrations.model.enums.Gender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MetricBenchmarkRepository extends JpaRepository<MetricBenchmark, UUID> {

    List<MetricBenchmark> findByMetricId(UUID metricId);

    @Query("SELECT b FROM MetricBenchmark b WHERE b.metric.id = :metricId " +
            "AND (b.ageCategory = :ageCategory OR b.ageCategory IS NULL) " +
            "AND (b.gender = :gender OR b.gender IS NULL) " +
            "AND b.minValue <= :rawValue AND b.maxValue >= :rawValue " +
            "ORDER BY b.score DESC")
    List<MetricBenchmark> findMatchingBenchmarks(
            @Param("metricId") UUID metricId,
            @Param("ageCategory") AgeCategory ageCategory,
            @Param("gender") Gender gender,
            @Param("rawValue") BigDecimal rawValue);
}
