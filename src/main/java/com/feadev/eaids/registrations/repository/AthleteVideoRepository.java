package com.feadev.eaids.registrations.repository;

import com.feadev.eaids.registrations.model.AthleteVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AthleteVideoRepository extends JpaRepository<AthleteVideo, UUID> {

    int countByAthleteProfileId(UUID athleteProfileId);

    Optional<AthleteVideo> findByIdAndAthleteProfile_User_Id(UUID id, UUID userId);
}
