package com.feadev.eaids.registrations.repository;

import com.feadev.eaids.registrations.model.AthleteProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AthleteProfileRepository extends JpaRepository<AthleteProfile, UUID> {

    Optional<AthleteProfile> findByUser_Id(UUID userId);

    boolean existsByUser_Id(UUID userId);
}
