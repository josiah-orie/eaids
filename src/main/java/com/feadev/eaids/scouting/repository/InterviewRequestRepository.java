package com.feadev.eaids.scouting.repository;

import com.feadev.eaids.scouting.model.InterviewRequest;
import com.feadev.eaids.scouting.model.enums.InterviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InterviewRequestRepository extends JpaRepository<InterviewRequest, UUID> {

    Page<InterviewRequest> findByStatus(InterviewStatus status, Pageable pageable);

    List<InterviewRequest> findByScoutUserIdOrderByCreatedDateDesc(UUID scoutUserId);

    Page<InterviewRequest> findByScoutUserId(UUID scoutUserId, Pageable pageable);
}
