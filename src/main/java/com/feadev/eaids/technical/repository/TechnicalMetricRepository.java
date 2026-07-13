package com.feadev.eaids.technical.repository;

import com.feadev.eaids.technical.model.TechnicalMetric;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TechnicalMetricRepository extends JpaRepository<TechnicalMetric, UUID> {

    List<TechnicalMetric> findByCampId(UUID campId);

    List<TechnicalMetric> findByCampIdAndPosition(UUID campId, FootballPosition position);
}
