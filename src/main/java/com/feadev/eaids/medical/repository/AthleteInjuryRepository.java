package com.feadev.eaids.medical.repository;

import com.feadev.eaids.medical.model.AthleteInjury;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AthleteInjuryRepository extends JpaRepository<AthleteInjury, UUID> {

    Page<AthleteInjury> findByAthleteUserId(UUID athleteUserId, Pageable pageable);

    /** Non-paginated variant used by the injury risk engine. */
    List<AthleteInjury> findByAthleteUserId(UUID athleteUserId);

    List<AthleteInjury> findByAthleteUserIdAndStatus(UUID athleteUserId, String status);

    List<AthleteInjury> findByAthleteUserIdAndIsSuspectedConcussionTrue(UUID athleteUserId);

    /** Used by the trend service to scope injuries to a time window. */
    List<AthleteInjury> findByIncidentDateBetween(LocalDateTime start, LocalDateTime end);

    /** Filter by a specific set of athlete IDs — used when scoping trends to a team or league. */
    @Query("SELECT i FROM AthleteInjury i WHERE i.athleteUserId IN :athleteIds")
    List<AthleteInjury> findByAthleteUserIdIn(@Param("athleteIds") List<UUID> athleteIds);

    /** Time-windowed query scoped to a list of athletes — for team/league trend filtering. */
    @Query("SELECT i FROM AthleteInjury i " +
           "WHERE i.athleteUserId IN :athleteIds " +
           "AND i.incidentDate BETWEEN :start AND :end")
    List<AthleteInjury> findByAthleteUserIdInAndIncidentDateBetween(
            @Param("athleteIds") List<UUID> athleteIds,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
