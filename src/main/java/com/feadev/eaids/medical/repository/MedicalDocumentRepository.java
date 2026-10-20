package com.feadev.eaids.medical.repository;

import com.feadev.eaids.medical.model.MedicalDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicalDocumentRepository extends JpaRepository<MedicalDocument, UUID> {

    Page<MedicalDocument> findByAthleteUserId(UUID athleteUserId, Pageable pageable);

    List<MedicalDocument> findByRelatedAssessmentId(UUID assessmentId);

    List<MedicalDocument> findByRelatedInjuryId(UUID injuryId);
}
