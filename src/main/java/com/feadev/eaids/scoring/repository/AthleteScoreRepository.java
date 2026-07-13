package com.feadev.eaids.scoring.repository;

import com.feadev.eaids.scoring.model.AthleteScore;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AthleteScoreRepository extends JpaRepository<AthleteScore, UUID> {

    Optional<AthleteScore> findByEnrollmentId(UUID enrollmentId);

    List<AthleteScore> findByCampIdOrderByTotalWeightedScoreDesc(UUID campId);

    List<AthleteScore> findByCampIdAndPosition(UUID campId, FootballPosition position);
}
