package com.feadev.eaids.registrations.repository;

import com.feadev.eaids.registrations.model.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, UUID> {

    Optional<OtpToken> findByUser_IdAndUsedFalse(UUID userId);
}
