package com.feadev.eaids.evaluation.repository;

import com.feadev.eaids.evaluation.model.CoachEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface CoachEvaluationRepository extends JpaRepository<CoachEvaluation, UUID> {

    List<CoachEvaluation> findByEnrollmentId(UUID enrollmentId);

    List<CoachEvaluation> findByEvaluatorId(UUID evaluatorId);

    @Query("SELECT AVG((e.technicalScore + e.tacticalScore + e.leadershipScore + e.characterScore) / 4) " +
            "FROM CoachEvaluation e WHERE e.enrollment.id = :enrollmentId")
    BigDecimal calculateAverageOverallScore(@Param("enrollmentId") UUID enrollmentId);
}
