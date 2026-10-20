package com.feadev.eaids.league.repository;

import com.feadev.eaids.league.model.PlayerTransfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlayerTransferRepository extends JpaRepository<PlayerTransfer, UUID> {
    List<PlayerTransfer> findByAthleteUserIdOrderByTransferDateDesc(UUID athleteUserId);
}
