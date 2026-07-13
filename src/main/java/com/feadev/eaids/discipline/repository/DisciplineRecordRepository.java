package com.feadev.eaids.discipline.repository;

import com.feadev.eaids.discipline.model.DisciplineRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DisciplineRecordRepository extends JpaRepository<DisciplineRecord, UUID> {

    List<DisciplineRecord> findByEnrollmentId(UUID enrollmentId);

    List<DisciplineRecord> findByCampId(UUID campId);

    @Query("SELECT COALESCE(SUM(d.penaltyPoints), 0) FROM DisciplineRecord d WHERE d.enrollment.id = :enrollmentId")
    int sumPenaltyPointsByEnrollmentId(@Param("enrollmentId") UUID enrollmentId);
}
