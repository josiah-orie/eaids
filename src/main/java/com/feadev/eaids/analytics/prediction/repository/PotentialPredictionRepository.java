package com.feadev.eaids.analytics.prediction.repository;

import com.feadev.eaids.analytics.prediction.model.PotentialPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PotentialPredictionRepository extends JpaRepository<PotentialPrediction, UUID> {

    Optional<PotentialPrediction> findTopByAthleteUserIdOrderByCalculatedAtDesc(UUID athleteUserId);

    List<PotentialPrediction> findByAthleteUserIdOrderByCalculatedAtDesc(UUID athleteUserId);
}
