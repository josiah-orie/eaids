package com.feadev.eaids.analytics.injuryrisk.repository;

import com.feadev.eaids.analytics.injuryrisk.model.InjuryRiskAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InjuryRiskRepository extends JpaRepository<InjuryRiskAssessment, UUID> {

    Optional<InjuryRiskAssessment> findTopByEnrollmentIdOrderByAssessedAtDesc(UUID enrollmentId);

    List<InjuryRiskAssessment> findByEnrollmentIdOrderByAssessedAtDesc(UUID enrollmentId);

    List<InjuryRiskAssessment> findByAthleteUserIdOrderByAssessedAtDesc(UUID athleteUserId);
}
