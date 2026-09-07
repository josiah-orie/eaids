package com.feadev.eaids.academy.repository;

import com.feadev.eaids.academy.model.AcademyScheduleStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AcademyScheduleStaffRepository extends JpaRepository<AcademyScheduleStaff, UUID> {

    boolean existsByScheduleIdAndCoachProfileId(UUID scheduleId, UUID coachProfileId);

    boolean existsByScheduleIdAndScoutProfileId(UUID scheduleId, UUID scoutProfileId);

    List<AcademyScheduleStaff> findByScheduleId(UUID scheduleId);
}
