package com.feadev.eaids.tactical.repository;

import com.feadev.eaids.tactical.model.TacticalMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TacticalMetricRepository extends JpaRepository<TacticalMetric, UUID> {

    List<TacticalMetric> findByCampId(UUID campId);
}
