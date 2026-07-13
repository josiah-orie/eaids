package com.feadev.eaids.physical.repository;

import com.feadev.eaids.physical.model.AthleteMeasurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AthleteMeasurementRepository extends JpaRepository<AthleteMeasurement, UUID> {

    List<AthleteMeasurement> findByEnrollmentId(UUID enrollmentId);

    @Query("SELECT AVG(m.calculatedScore) FROM AthleteMeasurement m WHERE m.enrollment.id = :enrollmentId AND m.calculatedScore IS NOT NULL")
    Double calculateAverageScoreByEnrollmentId(@Param("enrollmentId") UUID enrollmentId);
}
