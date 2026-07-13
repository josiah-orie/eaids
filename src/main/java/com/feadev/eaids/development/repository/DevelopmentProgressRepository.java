package com.feadev.eaids.development.repository;

import com.feadev.eaids.development.model.DevelopmentProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DevelopmentProgressRepository extends JpaRepository<DevelopmentProgress, UUID> {

    List<DevelopmentProgress> findByPlanIdOrderByRecordedDateDesc(UUID planId);

    List<DevelopmentProgress> findByGoalId(UUID goalId);
}
