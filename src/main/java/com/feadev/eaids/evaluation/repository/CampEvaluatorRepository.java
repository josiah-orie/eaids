package com.feadev.eaids.evaluation.repository;

import com.feadev.eaids.evaluation.model.CampEvaluator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampEvaluatorRepository extends JpaRepository<CampEvaluator, UUID> {

    List<CampEvaluator> findByCampId(UUID campId);

    List<CampEvaluator> findByUserId(UUID userId);

    Optional<CampEvaluator> findByCampIdAndUserId(UUID campId, UUID userId);

    boolean existsByCampIdAndUserId(UUID campId, UUID userId);
}
