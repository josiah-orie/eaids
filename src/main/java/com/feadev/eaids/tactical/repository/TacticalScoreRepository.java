package com.feadev.eaids.tactical.repository;

import com.feadev.eaids.tactical.model.TacticalScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TacticalScoreRepository extends JpaRepository<TacticalScore, UUID> {

    List<TacticalScore> findByEnrollmentId(UUID enrollmentId);
}
