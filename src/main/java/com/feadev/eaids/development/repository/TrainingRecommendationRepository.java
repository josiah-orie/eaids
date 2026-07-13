package com.feadev.eaids.development.repository;

import com.feadev.eaids.development.model.TrainingRecommendation;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TrainingRecommendationRepository extends JpaRepository<TrainingRecommendation, UUID> {

    List<TrainingRecommendation> findByPositionAndCategoryAndIsActiveTrue(FootballPosition position, String category);

    List<TrainingRecommendation> findByPositionAndIsActiveTrue(FootballPosition position);

    List<TrainingRecommendation> findByCategoryAndIsActiveTrue(String category);

    List<TrainingRecommendation> findByIsActiveTrue();
}
