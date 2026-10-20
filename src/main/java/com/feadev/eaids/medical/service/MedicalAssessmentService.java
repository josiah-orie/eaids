package com.feadev.eaids.medical.service;

import com.feadev.eaids.medical.dto.request.CreateAssessmentRequest;
import com.feadev.eaids.medical.dto.request.UpdateAssessmentRequest;
import com.feadev.eaids.medical.dto.response.AssessmentSummaryResponse;
import com.feadev.eaids.medical.dto.response.MedicalAssessmentResponse;
import com.feadev.eaids.medical.model.enums.AssessmentStatus;
import com.feadev.eaids.registrations.model.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MedicalAssessmentService {

    MedicalAssessmentResponse createAssessment(UUID athleteUserId,
                                                CreateAssessmentRequest request,
                                                UUID clinicianUserId,
                                                UserRole actorRole);

    MedicalAssessmentResponse updateAssessment(UUID assessmentId,
                                                UpdateAssessmentRequest request,
                                                UUID actorId,
                                                UserRole actorRole);

    MedicalAssessmentResponse transitionStatus(UUID assessmentId,
                                                AssessmentStatus newStatus,
                                                UUID actorId,
                                                UserRole actorRole);

    /**
     * Returns full response for MO/admin/physio; summary for athlete (own only).
     * Throws MedicalAccessException for coach/scout.
     */
    Object getAssessment(UUID assessmentId, UUID actorId, UserRole actorRole);

    Page<AssessmentSummaryResponse> getAssessmentSummariesForAthlete(UUID athleteUserId,
                                                                      UUID actorId,
                                                                      UserRole actorRole,
                                                                      Pageable pageable);

    Page<MedicalAssessmentResponse> getFullAssessmentsForAthlete(UUID athleteUserId,
                                                                   UUID actorId,
                                                                   UserRole actorRole,
                                                                   Pageable pageable);
}
