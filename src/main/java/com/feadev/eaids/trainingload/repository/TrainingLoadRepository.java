package com.feadev.eaids.trainingload.repository;

import com.feadev.eaids.trainingload.model.TrainingLoadRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TrainingLoadRepository extends JpaRepository<TrainingLoadRecord, UUID> {

    Page<TrainingLoadRecord> findByEnrollmentIdOrderBySessionDateDesc(UUID enrollmentId, Pageable pageable);

    List<TrainingLoadRecord> findByEnrollmentIdAndSessionDateBetweenOrderBySessionDateDesc(
            UUID enrollmentId, LocalDate startDate, LocalDate endDate);

    List<TrainingLoadRecord> findByCampIdOrderBySessionDateDesc(UUID campId);

    @Query("SELECT t FROM TrainingLoadRecord t WHERE t.enrollmentId = :enrollmentId " +
            "AND t.sessionDate >= :startDate ORDER BY t.sessionDate DESC")
    List<TrainingLoadRecord> findRecentByEnrollmentId(
            @Param("enrollmentId") UUID enrollmentId,
            @Param("startDate") LocalDate startDate);
}
