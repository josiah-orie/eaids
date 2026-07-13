package com.feadev.eaids.selection.repository;

import com.feadev.eaids.selection.model.SelectionDecision;
import com.feadev.eaids.selection.model.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SelectionDecisionRepository extends JpaRepository<SelectionDecision, UUID> {

    List<SelectionDecision> findByCampId(UUID campId);

    List<SelectionDecision> findByCampIdAndApprovalStatus(UUID campId, ApprovalStatus approvalStatus);

    Optional<SelectionDecision> findByEnrollmentId(UUID enrollmentId);
}
