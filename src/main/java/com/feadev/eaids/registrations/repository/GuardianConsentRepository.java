package com.feadev.eaids.registrations.repository;

import com.feadev.eaids.registrations.model.GuardianConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GuardianConsentRepository extends JpaRepository<GuardianConsent, UUID> {

    Optional<GuardianConsent> findByUser_Id(UUID userId);
}
