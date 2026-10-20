package com.feadev.eaids.medical.service.impl;

import com.feadev.eaids.audit.AuditInterceptor;
import com.feadev.eaids.medical.dto.request.CreateAssessmentRequest;
import com.feadev.eaids.medical.dto.request.UpdateAssessmentRequest;
import com.feadev.eaids.medical.dto.response.AssessmentSummaryResponse;
import com.feadev.eaids.medical.dto.response.MedicalAssessmentResponse;
import com.feadev.eaids.medical.model.MedicalAssessment;
import com.feadev.eaids.medical.model.enums.AssessmentStatus;
import com.feadev.eaids.medical.repository.MedicalAssessmentRepository;
import com.feadev.eaids.medical.service.InjuryRiskService;
import com.feadev.eaids.medical.service.MedicalAssessmentService;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.exception.MedicalAccessException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicalAssessmentServiceImpl implements MedicalAssessmentService {

    private final MedicalAssessmentRepository assessmentRepository;
    private final AuditService auditService;
    private final AuditInterceptor auditInterceptor;
    private final InjuryRiskService injuryRiskService;

    private static final Map<AssessmentStatus, Set<AssessmentStatus>> VALID_TRANSITIONS = Map.of(
            AssessmentStatus.DRAFT,       Set.of(AssessmentStatus.SUBMITTED),
            AssessmentStatus.SUBMITTED,   Set.of(AssessmentStatus.UNDER_REVIEW),
            AssessmentStatus.UNDER_REVIEW,Set.of(AssessmentStatus.FINALISED),
            AssessmentStatus.FINALISED,   Set.of(AssessmentStatus.AMENDED),
            AssessmentStatus.AMENDED,     Set.of()
    );

    @Override
    public MedicalAssessmentResponse createAssessment(UUID athleteUserId,
                                                       CreateAssessmentRequest request,
                                                       UUID clinicianUserId,
                                                       UserRole actorRole) {
        requireClinicalWriteRole(actorRole);

        MedicalAssessment assessment = MedicalAssessment.builder()
                .athleteUserId(athleteUserId)
                .assessmentType(request.getAssessmentType())
                .assessmentDate(request.getAssessmentDate())
                .clinicianUserId(clinicianUserId)
                .clinicianCredentials(request.getClinicianCredentials())
                .bloodPressureSystolic(request.getBloodPressureSystolic())
                .bloodPressureDiastolic(request.getBloodPressureDiastolic())
                .pulseBpm(request.getPulseBpm())
                .cardiovascularFindings(request.getCardiovascularFindings())
                .respiratoryFindings(request.getRespiratoryFindings())
                .neurologicalFindings(request.getNeurologicalFindings())
                .musculoskeletalFindings(request.getMusculoskeletalFindings())
                .investigationsResults(request.getInvestigationsResults())
                .clinicalFindings(request.getClinicalFindings())
                .recommendations(request.getRecommendations())
                .participationRestrictions(request.getParticipationRestrictions())
                .referralsRequired(request.getReferralsRequired())
                .followUpDate(request.getFollowUpDate())
                .status(AssessmentStatus.DRAFT.name())
                .build();

        MedicalAssessment saved = assessmentRepository.save(assessment);
        log.info("Assessment created: id={} athlete={} by clinician={}", saved.getId(),
                athleteUserId, clinicianUserId);
        return mapToFullResponse(saved);
    }

    @Override
    public MedicalAssessmentResponse updateAssessment(UUID assessmentId,
                                                       UpdateAssessmentRequest request,
                                                       UUID actorId,
                                                       UserRole actorRole) {
        requireClinicalWriteRole(actorRole);
        MedicalAssessment assessment = findOrThrow(assessmentId);

        AssessmentStatus current = AssessmentStatus.valueOf(assessment.getStatus());
        if (current != AssessmentStatus.DRAFT && current != AssessmentStatus.SUBMITTED) {
            throw new InvalidStateTransitionException(
                    "Assessment can only be updated when DRAFT or SUBMITTED. Current: " + current);
        }

        if (request.getAssessmentDate() != null)          assessment.setAssessmentDate(request.getAssessmentDate());
        if (request.getClinicianCredentials() != null)    assessment.setClinicianCredentials(request.getClinicianCredentials());
        if (request.getBloodPressureSystolic() != null)   assessment.setBloodPressureSystolic(request.getBloodPressureSystolic());
        if (request.getBloodPressureDiastolic() != null)  assessment.setBloodPressureDiastolic(request.getBloodPressureDiastolic());
        if (request.getPulseBpm() != null)                assessment.setPulseBpm(request.getPulseBpm());
        if (request.getCardiovascularFindings() != null)  assessment.setCardiovascularFindings(request.getCardiovascularFindings());
        if (request.getRespiratoryFindings() != null)     assessment.setRespiratoryFindings(request.getRespiratoryFindings());
        if (request.getNeurologicalFindings() != null)    assessment.setNeurologicalFindings(request.getNeurologicalFindings());
        if (request.getMusculoskeletalFindings() != null) assessment.setMusculoskeletalFindings(request.getMusculoskeletalFindings());
        if (request.getInvestigationsResults() != null)   assessment.setInvestigationsResults(request.getInvestigationsResults());
        if (request.getClinicalFindings() != null)        assessment.setClinicalFindings(request.getClinicalFindings());
        if (request.getRecommendations() != null)         assessment.setRecommendations(request.getRecommendations());
        if (request.getParticipationRestrictions() != null) assessment.setParticipationRestrictions(request.getParticipationRestrictions());
        if (request.getReferralsRequired() != null)       assessment.setReferralsRequired(request.getReferralsRequired());
        if (request.getFollowUpDate() != null)            assessment.setFollowUpDate(request.getFollowUpDate());

        return mapToFullResponse(assessmentRepository.save(assessment));
    }

    @Override
    public MedicalAssessmentResponse transitionStatus(UUID assessmentId,
                                                       AssessmentStatus newStatus,
                                                       UUID actorId,
                                                       UserRole actorRole) {
        requireClinicalWriteRole(actorRole);
        MedicalAssessment assessment = findOrThrow(assessmentId);

        AssessmentStatus current = AssessmentStatus.valueOf(assessment.getStatus());
        Set<AssessmentStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidStateTransitionException(
                    String.format("Cannot transition assessment from %s to %s. Allowed: %s",
                            current, newStatus, allowed));
        }

        assessment.setStatus(newStatus.name());
        if (newStatus == AssessmentStatus.FINALISED) {
            assessment.setFinalisedAt(LocalDateTime.now());
            auditService.log(AuditAction.MEDICAL_ASSESSMENT_FINALISED, actorId, actorId.toString(),
                    auditInterceptor.getClientIpAddress(), auditInterceptor.getUserAgent());
            // Evict cached risk score — finalised assessment may have updated conditions/restrictions
            injuryRiskService.evictRiskCache(assessment.getAthleteUserId());
        }

        return mapToFullResponse(assessmentRepository.save(assessment));
    }

    @Override
    @Transactional(readOnly = true)
    public Object getAssessment(UUID assessmentId, UUID actorId, UserRole actorRole) {
        MedicalAssessment assessment = findOrThrow(assessmentId);
        return switch (actorRole) {
            case ROLE_MEDICAL_OFFICER, ROLE_ADMIN, ROLE_PHYSIOTHERAPIST -> mapToFullResponse(assessment);
            case ROLE_ATHLETE -> {
                if (!assessment.getAthleteUserId().equals(actorId)) {
                    throw new MedicalAccessException("Athletes may only view their own assessments");
                }
                yield mapToSummaryResponse(assessment);
            }
            default -> throw new MedicalAccessException(
                    "Your role does not permit access to medical assessments");
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AssessmentSummaryResponse> getAssessmentSummariesForAthlete(UUID athleteUserId,
                                                                              UUID actorId,
                                                                              UserRole actorRole,
                                                                              Pageable pageable) {
        if (actorRole == UserRole.ROLE_ATHLETE && !athleteUserId.equals(actorId)) {
            throw new MedicalAccessException("Athletes may only view their own assessments");
        }
        if (actorRole == UserRole.ROLE_COACH || actorRole == UserRole.ROLE_SCOUT) {
            throw new MedicalAccessException("Coaches and scouts cannot access medical assessments");
        }
        return assessmentRepository.findByAthleteUserId(athleteUserId, pageable)
                .map(this::mapToSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalAssessmentResponse> getFullAssessmentsForAthlete(UUID athleteUserId,
                                                                          UUID actorId,
                                                                          UserRole actorRole,
                                                                          Pageable pageable) {
        requireClinicalReadRole(actorRole);
        return assessmentRepository.findByAthleteUserId(athleteUserId, pageable)
                .map(this::mapToFullResponse);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private MedicalAssessment findOrThrow(UUID id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical assessment not found with id: " + id));
    }

    private void requireClinicalWriteRole(UserRole role) {
        if (role != UserRole.ROLE_MEDICAL_OFFICER && role != UserRole.ROLE_ADMIN) {
            throw new MedicalAccessException(
                    "Only medical officers and admins may create or modify medical assessments");
        }
    }

    private void requireClinicalReadRole(UserRole role) {
        if (role == UserRole.ROLE_COACH || role == UserRole.ROLE_SCOUT
                || role == UserRole.ROLE_ATHLETE) {
            throw new MedicalAccessException(
                    "Your role does not permit full access to medical assessments");
        }
    }

    private MedicalAssessmentResponse mapToFullResponse(MedicalAssessment a) {
        return MedicalAssessmentResponse.builder()
                .id(a.getId())
                .athleteUserId(a.getAthleteUserId())
                .assessmentType(a.getAssessmentType())
                .assessmentDate(a.getAssessmentDate())
                .clinicianUserId(a.getClinicianUserId())
                .clinicianCredentials(a.getClinicianCredentials())
                .bloodPressureSystolic(a.getBloodPressureSystolic())
                .bloodPressureDiastolic(a.getBloodPressureDiastolic())
                .pulseBpm(a.getPulseBpm())
                .cardiovascularFindings(a.getCardiovascularFindings())
                .respiratoryFindings(a.getRespiratoryFindings())
                .neurologicalFindings(a.getNeurologicalFindings())
                .musculoskeletalFindings(a.getMusculoskeletalFindings())
                .investigationsResults(a.getInvestigationsResults())
                .clinicalFindings(a.getClinicalFindings())
                .recommendations(a.getRecommendations())
                .participationRestrictions(a.getParticipationRestrictions())
                .referralsRequired(a.getReferralsRequired())
                .followUpDate(a.getFollowUpDate())
                .status(a.getStatus())
                .finalisedAt(a.getFinalisedAt())
                .createdDate(a.getCreatedDate())
                .lastModifiedDate(a.getLastModifiedDate())
                .build();
    }

    private AssessmentSummaryResponse mapToSummaryResponse(MedicalAssessment a) {
        return AssessmentSummaryResponse.builder()
                .id(a.getId())
                .assessmentType(a.getAssessmentType())
                .assessmentDate(a.getAssessmentDate())
                .status(a.getStatus())
                .followUpDate(a.getFollowUpDate())
                .participationRestrictions(a.getParticipationRestrictions())
                .createdDate(a.getCreatedDate())
                .build();
    }
}
