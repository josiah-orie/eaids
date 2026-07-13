package com.feadev.eaids.assessment.repository;

import com.feadev.eaids.assessment.model.CampStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CampStageRepository extends JpaRepository<CampStage, UUID> {

    List<CampStage> findByCampIdOrderByStageOrderAsc(UUID campId);

    List<CampStage> findByCampIdAndIsActiveTrueOrderByStageOrderAsc(UUID campId);

    int countByCampId(UUID campId);
}
