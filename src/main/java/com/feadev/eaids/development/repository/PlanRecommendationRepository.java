package com.feadev.eaids.development.repository;

import com.feadev.eaids.development.model.PlanRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlanRecommendationRepository extends JpaRepository<PlanRecommendation, UUID> {

    List<PlanRecommendation> findByPlanIdOrderByPriorityAsc(UUID planId);
}
