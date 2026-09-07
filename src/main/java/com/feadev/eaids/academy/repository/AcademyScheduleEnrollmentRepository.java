package com.feadev.eaids.academy.repository;

import com.feadev.eaids.academy.model.AcademyScheduleEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AcademyScheduleEnrollmentRepository
        extends JpaRepository<AcademyScheduleEnrollment, UUID> {

    boolean existsByScheduleIdAndIndividualApplicationId(UUID scheduleId, UUID individualApplicationId);

    boolean existsByScheduleIdAndInstitutionApplicationId(UUID scheduleId, UUID institutionApplicationId);

    long countByScheduleId(UUID scheduleId);

    List<AcademyScheduleEnrollment> findByScheduleId(UUID scheduleId);
}
