package com.feadev.eaids.positionprofile.repository;

import com.feadev.eaids.positionprofile.model.CampPositionProfile;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampPositionProfileRepository extends JpaRepository<CampPositionProfile, UUID> {

    List<CampPositionProfile> findByCampId(UUID campId);

    boolean existsByCampIdAndPositionProfileId(UUID campId, UUID positionProfileId);

    Optional<CampPositionProfile> findByCampIdAndPositionProfileId(UUID campId, UUID positionProfileId);

    @Query("SELECT cpp FROM CampPositionProfile cpp WHERE cpp.camp.id = :campId AND cpp.positionProfile.position = :position AND cpp.positionProfile.isActive = true")
    Optional<CampPositionProfile> findByCampIdAndPosition(@Param("campId") UUID campId, @Param("position") FootballPosition position);

    void deleteByCampIdAndPositionProfileId(UUID campId, UUID positionProfileId);
}
