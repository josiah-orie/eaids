package com.feadev.eaids.medical.repository;

import com.feadev.eaids.medical.model.AthleteMedicalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AthleteMedicalProfileRepository extends JpaRepository<AthleteMedicalProfile, UUID> {

    Optional<AthleteMedicalProfile> findByAthleteUserId(UUID athleteUserId);

    boolean existsByAthleteUserId(UUID athleteUserId);
}
