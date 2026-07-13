package com.feadev.eaids.scoring.repository;

import com.feadev.eaids.scoring.model.CampWeightProfile;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampWeightProfileRepository extends JpaRepository<CampWeightProfile, UUID> {

    List<CampWeightProfile> findByCampId(UUID campId);

    Optional<CampWeightProfile> findByCampIdAndPosition(UUID campId, FootballPosition position);
}
