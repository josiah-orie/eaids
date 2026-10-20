package com.feadev.eaids.medical.service.impl;

import com.feadev.eaids.audit.AuditInterceptor;
import com.feadev.eaids.medical.dto.request.UpdateMedicalProfileRequest;
import com.feadev.eaids.medical.dto.response.MedicalProfileResponse;
import com.feadev.eaids.medical.model.AthleteMedicalProfile;
import com.feadev.eaids.medical.repository.AthleteMedicalProfileRepository;
import com.feadev.eaids.medical.service.MedicalProfileService;
import com.feadev.eaids.registrations.exception.MedicalAccessException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicalProfileServiceImpl implements MedicalProfileService {

    private final AthleteMedicalProfileRepository profileRepository;
    private final AuditService auditService;
    private final AuditInterceptor auditInterceptor;

    @Override
    public MedicalProfileResponse createOrUpdateProfile(UUID athleteUserId,
                                                         UpdateMedicalProfileRequest request,
                                                         UUID actorId,
                                                         UserRole actorRole) {
        enforceWriteAccess(athleteUserId, actorId, actorRole);

        AthleteMedicalProfile profile = profileRepository.findByAthleteUserId(athleteUserId)
                .orElseGet(() -> AthleteMedicalProfile.builder()
                        .athleteUserId(athleteUserId)
                        .build());

        boolean isNew = profile.getId() == null;

        applyUpdates(profile, request, actorId);
        AthleteMedicalProfile saved = profileRepository.save(profile);

        AuditAction action = isNew ? AuditAction.MEDICAL_PROFILE_CREATED
                                   : AuditAction.MEDICAL_PROFILE_UPDATED;
        auditService.log(action, actorId, actorId.toString(),
                auditInterceptor.getClientIpAddress(), auditInterceptor.getUserAgent());

        log.info("Medical profile {} for athlete={} by actor={}", isNew ? "created" : "updated",
                athleteUserId, actorId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalProfileResponse getProfile(UUID athleteUserId, UUID actorId, UserRole actorRole) {
        enforceReadAccess(athleteUserId, actorId, actorRole);

        AthleteMedicalProfile profile = profileRepository.findByAthleteUserId(athleteUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical profile not found for athlete: " + athleteUserId));
        return mapToResponse(profile);
    }

    // -----------------------------------------------------------------------
    // Access control
    // -----------------------------------------------------------------------

    private void enforceReadAccess(UUID athleteUserId, UUID actorId, UserRole actorRole) {
        if (actorRole == UserRole.ROLE_ATHLETE && !athleteUserId.equals(actorId)) {
            throw new MedicalAccessException("Athletes may only view their own medical profile");
        }
        if (actorRole == UserRole.ROLE_COACH || actorRole == UserRole.ROLE_SCOUT) {
            throw new MedicalAccessException(
                    "Coaches and scouts do not have access to athlete medical profiles");
        }
    }

    private void enforceWriteAccess(UUID athleteUserId, UUID actorId, UserRole actorRole) {
        if (actorRole == UserRole.ROLE_ATHLETE && !athleteUserId.equals(actorId)) {
            throw new MedicalAccessException("Athletes may only update their own medical profile");
        }
        if (actorRole == UserRole.ROLE_COACH || actorRole == UserRole.ROLE_SCOUT
                || actorRole == UserRole.ROLE_PHYSIOTHERAPIST) {
            throw new MedicalAccessException(
                    "Your role does not permit updating athlete medical profiles");
        }
    }

    // -----------------------------------------------------------------------
    // Field mapping
    // -----------------------------------------------------------------------

    private void applyUpdates(AthleteMedicalProfile profile,
                               UpdateMedicalProfileRequest req,
                               UUID actorId) {
        if (req.getBloodType() != null)                   profile.setBloodType(req.getBloodType());
        if (req.getExistingConditions() != null)          profile.setExistingConditions(req.getExistingConditions());
        if (req.getPastIllnesses() != null)               profile.setPastIllnesses(req.getPastIllnesses());
        if (req.getPreviousSurgeries() != null)           profile.setPreviousSurgeries(req.getPreviousSurgeries());
        if (req.getHospitalAdmissions() != null)          profile.setHospitalAdmissions(req.getHospitalAdmissions());
        if (req.getKnownAllergies() != null)              profile.setKnownAllergies(req.getKnownAllergies());
        if (req.getCurrentMedications() != null)          profile.setCurrentMedications(req.getCurrentMedications());
        if (req.getFamilyCardiacHistory() != null)        profile.setFamilyCardiacHistory(req.getFamilyCardiacHistory());
        if (req.getExerciseSymptoms() != null)            profile.setExerciseSymptoms(req.getExerciseSymptoms());
        if (req.getVisionHearingNotes() != null)          profile.setVisionHearingNotes(req.getVisionHearingNotes());
        if (req.getMusculoskeletalNotes() != null)        profile.setMusculoskeletalNotes(req.getMusculoskeletalNotes());
        if (req.getEmergencyContactName() != null)        profile.setEmergencyContactName(req.getEmergencyContactName());
        if (req.getEmergencyContactPhone() != null)       profile.setEmergencyContactPhone(req.getEmergencyContactPhone());
        if (req.getEmergencyContactRelation() != null)    profile.setEmergencyContactRelation(req.getEmergencyContactRelation());
        if (req.getNeurologicalSpinalHistory() != null)   profile.setNeurologicalSpinalHistory(req.getNeurologicalSpinalHistory());
        if (req.getFunctionalLimitations() != null)       profile.setFunctionalLimitations(req.getFunctionalLimitations());
        if (req.getAssistiveEquipment() != null)          profile.setAssistiveEquipment(req.getAssistiveEquipment());
        if (req.getIndividualEmergencyCareNotes() != null)profile.setIndividualEmergencyCareNotes(req.getIndividualEmergencyCareNotes());
        if (req.getOutstandingClarifications() != null)   profile.setOutstandingClarifications(req.getOutstandingClarifications());

        if (Boolean.TRUE.equals(req.getQuestionnaireCompleted())) {
            profile.setQuestionnaireCompleted(true);
            if (profile.getSubmittedBy() == null) {
                profile.setSubmittedBy(actorId);
                profile.setSubmittedDate(LocalDateTime.now());
            }
        } else if (req.getQuestionnaireCompleted() != null) {
            profile.setQuestionnaireCompleted(req.getQuestionnaireCompleted());
        }
    }

    private MedicalProfileResponse mapToResponse(AthleteMedicalProfile p) {
        return MedicalProfileResponse.builder()
                .id(p.getId())
                .athleteUserId(p.getAthleteUserId())
                .bloodType(p.getBloodType())
                .existingConditions(p.getExistingConditions())
                .pastIllnesses(p.getPastIllnesses())
                .previousSurgeries(p.getPreviousSurgeries())
                .hospitalAdmissions(p.getHospitalAdmissions())
                .knownAllergies(p.getKnownAllergies())
                .currentMedications(p.getCurrentMedications())
                .familyCardiacHistory(p.isFamilyCardiacHistory())
                .exerciseSymptoms(p.getExerciseSymptoms())
                .visionHearingNotes(p.getVisionHearingNotes())
                .musculoskeletalNotes(p.getMusculoskeletalNotes())
                .emergencyContactName(p.getEmergencyContactName())
                .emergencyContactPhone(p.getEmergencyContactPhone())
                .emergencyContactRelation(p.getEmergencyContactRelation())
                .neurologicalSpinalHistory(p.getNeurologicalSpinalHistory())
                .functionalLimitations(p.getFunctionalLimitations())
                .assistiveEquipment(p.getAssistiveEquipment())
                .individualEmergencyCareNotes(p.getIndividualEmergencyCareNotes())
                .questionnaireCompleted(p.isQuestionnaireCompleted())
                .reviewedByMedicalOfficer(p.isReviewedByMedicalOfficer())
                .submittedBy(p.getSubmittedBy())
                .submittedDate(p.getSubmittedDate())
                .lastReviewedDate(p.getLastReviewedDate())
                .outstandingClarifications(p.getOutstandingClarifications())
                .createdDate(p.getCreatedDate())
                .lastModifiedDate(p.getLastModifiedDate())
                .build();
    }
}
