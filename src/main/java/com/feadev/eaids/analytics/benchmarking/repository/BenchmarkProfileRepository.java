package com.feadev.eaids.analytics.benchmarking.repository;

import com.feadev.eaids.analytics.benchmarking.model.BenchmarkProfile;
import com.feadev.eaids.analytics.benchmarking.model.enums.BenchmarkType;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BenchmarkProfileRepository extends JpaRepository<BenchmarkProfile, UUID> {

    List<BenchmarkProfile> findByBenchmarkType(BenchmarkType type);

    List<BenchmarkProfile> findByBenchmarkTypeAndPosition(BenchmarkType type, FootballPosition position);

    List<BenchmarkProfile> findByPosition(FootballPosition position);

    boolean existsByCapturedFromEnrollmentId(UUID enrollmentId);
}
