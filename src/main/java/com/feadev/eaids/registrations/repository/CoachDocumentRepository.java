package com.feadev.eaids.registrations.repository;

import com.feadev.eaids.registrations.model.CoachDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CoachDocumentRepository extends JpaRepository<CoachDocument, UUID> {

    int countByCoachProfileId(UUID coachProfileId);
}
