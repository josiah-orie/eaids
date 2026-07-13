package com.feadev.eaids.positionprofile.repository;

import com.feadev.eaids.positionprofile.model.PositionProfile;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PositionProfileRepository extends JpaRepository<PositionProfile, UUID> {

    List<PositionProfile> findByIsActiveTrue();

    List<PositionProfile> findByPosition(FootballPosition position);

    List<PositionProfile> findByPositionAndIsActiveTrue(FootballPosition position);

    boolean existsByPositionAndIsActiveTrue(FootballPosition position);
}
