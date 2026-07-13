package com.feadev.eaids.development.repository;

import com.feadev.eaids.development.model.DevelopmentPlan;
import com.feadev.eaids.development.model.enums.DevelopmentPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DevelopmentPlanRepository extends JpaRepository<DevelopmentPlan, UUID> {

    List<DevelopmentPlan> findByAthleteUserId(UUID athleteUserId);

    List<DevelopmentPlan> findByAthleteUserIdAndStatus(UUID athleteUserId, DevelopmentPlanStatus status);

    List<DevelopmentPlan> findByCoachUserId(UUID coachUserId);

    List<DevelopmentPlan> findByCoachUserIdAndStatus(UUID coachUserId, DevelopmentPlanStatus status);

    List<DevelopmentPlan> findByStatus(DevelopmentPlanStatus status);

    List<DevelopmentPlan> findByCampId(UUID campId);

    long countByStatus(DevelopmentPlanStatus status);

    long countByCoachUserId(UUID coachUserId);
}
