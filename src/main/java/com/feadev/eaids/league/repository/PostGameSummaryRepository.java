package com.feadev.eaids.league.repository;

import com.feadev.eaids.league.model.PostGameSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostGameSummaryRepository extends JpaRepository<PostGameSummary, UUID> {

    List<PostGameSummary> findByFixtureId(UUID fixtureId);

    Page<PostGameSummary> findByAthleteUserId(UUID athleteUserId, Pageable pageable);

    Optional<PostGameSummary> findByFixtureIdAndTeamPlayerId(UUID fixtureId, UUID teamPlayerId);

    List<PostGameSummary> findByTeamIdAndFixtureId(UUID teamId, UUID fixtureId);
}
