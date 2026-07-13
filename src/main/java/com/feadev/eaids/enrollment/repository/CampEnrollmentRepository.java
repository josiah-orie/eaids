package com.feadev.eaids.enrollment.repository;

import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.model.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CampEnrollmentRepository extends JpaRepository<CampEnrollment, UUID> {

    boolean existsByCampIdAndUserId(UUID campId, UUID userId);

    List<CampEnrollment> findByCampId(UUID campId);

    List<CampEnrollment> findByCampIdAndStatus(UUID campId, EnrollmentStatus status);

    List<CampEnrollment> findByUserId(UUID userId);

    Optional<CampEnrollment> findByCampIdAndUserId(UUID campId, UUID userId);

    int countByCampIdAndStatusIn(UUID campId, List<EnrollmentStatus> statuses);
}
