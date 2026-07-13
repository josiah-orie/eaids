package com.feadev.eaids.technical.repository;

import com.feadev.eaids.technical.model.TechnicalScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TechnicalScoreRepository extends JpaRepository<TechnicalScore, UUID> {

    List<TechnicalScore> findByEnrollmentId(UUID enrollmentId);

    List<TechnicalScore> findByEnrollmentIdAndMetric_Position(
            UUID enrollmentId,
            com.feadev.eaids.technical.model.enums.FootballPosition position);
}
