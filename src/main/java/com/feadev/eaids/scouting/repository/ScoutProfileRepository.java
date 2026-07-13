package com.feadev.eaids.scouting.repository;

import com.feadev.eaids.scouting.model.ScoutProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScoutProfileRepository extends JpaRepository<ScoutProfile, UUID> {

    Optional<ScoutProfile> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);
}
