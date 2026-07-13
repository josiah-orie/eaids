package com.feadev.eaids.matchstats.repository;

import com.feadev.eaids.matchstats.model.MatchStatEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MatchStatEntryRepository extends JpaRepository<MatchStatEntry, UUID> {

    List<MatchStatEntry> findByEnrollmentId(UUID enrollmentId);

    List<MatchStatEntry> findByEnrollmentIdAndMatchDate(UUID enrollmentId, LocalDate matchDate);

    List<MatchStatEntry> findByCampId(UUID campId);

    @Query("SELECT e FROM MatchStatEntry e WHERE e.campId = :campId AND e.enrollment.id = :enrollmentId")
    List<MatchStatEntry> findByCampIdAndEnrollmentId(@Param("campId") UUID campId, @Param("enrollmentId") UUID enrollmentId);

    @Query("SELECT e FROM MatchStatEntry e WHERE e.campId = :campId AND e.definition.positionGroup = :positionGroup")
    List<MatchStatEntry> findByCampIdAndPositionGroup(@Param("campId") UUID campId, @Param("positionGroup") com.feadev.eaids.matchstats.model.enums.PositionGroup positionGroup);
}
