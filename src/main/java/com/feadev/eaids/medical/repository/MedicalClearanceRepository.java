package com.feadev.eaids.medical.repository;

import com.feadev.eaids.medical.model.MedicalClearance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicalClearanceRepository extends JpaRepository<MedicalClearance, UUID> {

    Page<MedicalClearance> findByAthleteUserId(UUID athleteUserId, Pageable pageable);

    List<MedicalClearance> findByAthleteUserIdAndStatus(UUID athleteUserId, String status);

    /** Non-paginated list of all clearances for an athlete — used by the injury risk engine. */
    List<MedicalClearance> findAllByAthleteUserId(UUID athleteUserId);

    @Query("SELECT c FROM MedicalClearance c WHERE c.athleteUserId = :athleteUserId " +
           "ORDER BY c.decisionDate DESC")
    Page<MedicalClearance> findLatestByAthleteUserId(
            @Param("athleteUserId") UUID athleteUserId, Pageable pageable);
}
