package com.feadev.eaids.physical.repository;

import com.feadev.eaids.physical.model.AssessmentMetric;
import com.feadev.eaids.physical.model.enums.MetricCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssessmentMetricRepository extends JpaRepository<AssessmentMetric, UUID> {

    List<AssessmentMetric> findByCampId(UUID campId);

    List<AssessmentMetric> findByCampIdAndCategory(UUID campId, MetricCategory category);

    List<AssessmentMetric> findByCampIdAndIsActiveTrue(UUID campId);
}
