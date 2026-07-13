package com.feadev.eaids.attendance.repository;

import com.feadev.eaids.attendance.model.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, UUID> {

    List<AttendanceRecord> findByEnrollmentId(UUID enrollmentId);

    List<AttendanceRecord> findByCampId(UUID campId);

    @Query("SELECT AVG(a.score) FROM AttendanceRecord a WHERE a.enrollment.id = :enrollmentId")
    BigDecimal calculateAverageScoreByEnrollmentId(@Param("enrollmentId") UUID enrollmentId);
}
