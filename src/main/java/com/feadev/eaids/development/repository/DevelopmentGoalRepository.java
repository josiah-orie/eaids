package com.feadev.eaids.development.repository;

import com.feadev.eaids.development.model.DevelopmentGoal;
import com.feadev.eaids.development.model.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DevelopmentGoalRepository extends JpaRepository<DevelopmentGoal, UUID> {

    List<DevelopmentGoal> findByPlanId(UUID planId);

    long countByPlanIdAndStatus(UUID planId, GoalStatus status);
}
