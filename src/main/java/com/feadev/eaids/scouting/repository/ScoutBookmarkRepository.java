package com.feadev.eaids.scouting.repository;

import com.feadev.eaids.scouting.model.ScoutBookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScoutBookmarkRepository extends JpaRepository<ScoutBookmark, UUID> {

    Page<ScoutBookmark> findByScoutUserIdOrderByCreatedDateDesc(UUID scoutUserId, Pageable pageable);

    Optional<ScoutBookmark> findByScoutUserIdAndAthleteUserId(UUID scoutUserId, UUID athleteUserId);

    boolean existsByScoutUserIdAndAthleteUserId(UUID scoutUserId, UUID athleteUserId);
}
