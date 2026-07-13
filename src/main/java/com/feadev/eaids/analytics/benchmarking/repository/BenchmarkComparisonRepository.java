package com.feadev.eaids.analytics.benchmarking.repository;

import com.feadev.eaids.analytics.benchmarking.model.BenchmarkComparison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BenchmarkComparisonRepository extends JpaRepository<BenchmarkComparison, UUID> {

    List<BenchmarkComparison> findByEnrollmentIdOrderByComparedAtDesc(UUID enrollmentId);
}
