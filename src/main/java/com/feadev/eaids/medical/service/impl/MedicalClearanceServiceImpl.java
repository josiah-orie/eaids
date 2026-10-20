package com.feadev.eaids.medical.service.impl;

import com.feadev.eaids.audit.AuditInterceptor;
import com.feadev.eaids.medical.dto.request.IssueClearanceRequest;
import com.feadev.eaids.medical.dto.request.RevokeClearanceRequest;
import com.feadev.eaids.medical.dto.response.MedicalClearanceResponse;
import com.feadev.eaids.medical.dto.response.ParticipationEligibilityResponse;
import com.feadev.eaids.medical.model.MedicalClearance;
import com.feadev.eaids.medical.model.enums.ClearanceStatus;
import com.feadev.eaids.medical.repository.MedicalClearanceRepository;
import com.feadev.eaids.medical.service.MedicalClearanceService;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.exception.MedicalAccessException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MedicalClearanceServiceImpl implements MedicalClearanceService {

    private final MedicalClearanceRepository clearanceRepository;
    private final AuditService auditService;
    private final AuditInterceptor auditInterceptor;

    private static final Map<ClearanceStatus, Set<ClearanceStatus>> VALID_TRANSITIONS = Map.of(
            ClearanceStatus.PENDING,
                    Set.of(ClearanceStatus.CLEARED, ClearanceStatus.CLEARED_WITH_RESTRICTIONS,
                            ClearanceStatus.NOT_CLEARED),
            ClearanceStatus.CLEARED,
                    Set.of(ClearanceStatus.EXPIRED, ClearanceStatus.REVOKED),
            ClearanceStatus.CLEARED_WITH_RESTRICTIONS,
                    Set.of(ClearanceStatus.EXPIRED, ClearanceStatus.REVOKED),
            ClearanceStatus.NOT_CLEARED,
                    Set.of(ClearanceStatus.REVOKED),
            ClearanceStatus.EXPIRED,
                    Set.of(ClearanceStatus.PENDING),
            ClearanceStatus.REVOKED,
                    Set.of()
    );

    @Override
    public MedicalClearanceResponse issueClearance(UUID athleteUserId,
                                                    IssueClearanceRequest request,
                                                    UUID actorId,
                                                    UserRole actorRole) {
        requireClinicalWriteRole(actorRole);

        // Athletes cannot issue their own clearance
        if (actorRole == UserRole.ROLE_ATHLETE) {
            throw new MedicalAccessException("Athletes may not issue their own medical clearance");
        }

        ClearanceStatus requestedStatus = ClearanceStatus.valueOf(request.getStatus().toUpperCase());
        if (requestedStatus == ClearanceStatus.PENDING
                || requestedStatus == ClearanceStatus.EXPIRED
                || requestedStatus == ClearanceStatus.REVOKED) {
            throw new InvalidStateTransitionException(
                    "Clearance can only be issued with status CLEARED, CLEARED_WITH_RESTRICTIONS, " +
                    "or NOT_CLEARED");
        }

        MedicalClearance clearance = MedicalClearance.builder()
                .athleteUserId(athleteUserId)
                .issuedBy(actorId)
                .relatedAssessmentId(request.getRelatedAssessmentId())
                .relatedInjuryId(request.getRelatedInjuryId())
                .activityScope(request.getActivityScope())
                .status(requestedStatus.name())
                .decisionDate(LocalDateTime.now())
                .effectiveDate(request.getEffectiveDate())
                .expiryDate(request.getExpiryDate())
                .participationRestrictions(request.getParticipationRestrictions())
                .requiredAccommodations(request.getRequiredAccommodations())
                .followUpRequirements(request.getFollowUpRequirements())
                .conditionalReason(request.getConditionalReason())
                .previousClearanceId(request.getPreviousClearanceId())
                .build();

        MedicalClearance saved = clearanceRepository.save(clearance);

        auditService.log(AuditAction.MEDICAL_CLEARANCE_ISSUED, actorId, actorId.toString(),
                auditInterceptor.getClientIpAddress(), auditInterceptor.getUserAgent());

        log.info("Clearance issued: id={} athlete={} status={} by={}",
                saved.getId(), athleteUserId, requestedStatus, actorId);
        return mapToResponse(saved);
    }

    @Override
    public MedicalClearanceResponse revokeClearance(UUID clearanceId,
                                                     RevokeClearanceRequest request,
                                                     UUID actorId,
                                                     UserRole actorRole) {
        requireClinicalWriteRole(actorRole);

        MedicalClearance clearance = findOrThrow(clearanceId);
        ClearanceStatus current = ClearanceStatus.valueOf(clearance.getStatus());
        Set<ClearanceStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());

        if (!allowed.contains(ClearanceStatus.REVOKED)) {
            throw new InvalidStateTransitionException(
                    "Cannot revoke a clearance with status: " + current);
        }

        clearance.setStatus(ClearanceStatus.REVOKED.name());
        clearance.setRevocationReason(request.getRevocationReason());
        clearance.setRevokedBy(actorId);
        clearance.setRevokedAt(LocalDateTime.now());

        MedicalClearance saved = clearanceRepository.save(clearance);

        auditService.log(AuditAction.MEDICAL_CLEARANCE_REVOKED, actorId, actorId.toString(),
                auditInterceptor.getClientIpAddress(), auditInterceptor.getUserAgent());

        log.info("Clearance revoked: id={} by={}", clearanceId, actorId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getClearance(UUID clearanceId, UUID actorId, UserRole actorRole) {
        MedicalClearance clearance = findOrThrow(clearanceId);
        return switch (actorRole) {
            case ROLE_MEDICAL_OFFICER, ROLE_ADMIN, ROLE_PHYSIOTHERAPIST -> mapToResponse(clearance);
            case ROLE_ATHLETE -> {
                if (!clearance.getAthleteUserId().equals(actorId)) {
                    throw new MedicalAccessException("Athletes may only view their own clearances");
                }
                yield mapToResponse(clearance);
            }
            case ROLE_COACH, ROLE_SCOUT -> mapToEligibility(clearance);
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MedicalClearanceResponse> getClearancesForAthlete(UUID athleteUserId,
                                                                    UUID actorId,
                                                                    UserRole actorRole,
                                                                    Pageable pageable) {
        if (actorRole == UserRole.ROLE_ATHLETE && !athleteUserId.equals(actorId)) {
            throw new MedicalAccessException("Athletes may only view their own clearances");
        }
        if (actorRole == UserRole.ROLE_COACH || actorRole == UserRole.ROLE_SCOUT) {
            throw new MedicalAccessException(
                    "Use the participation-eligibility endpoint for eligibility information");
        }
        return clearanceRepository.findByAthleteUserId(athleteUserId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ParticipationEligibilityResponse getParticipationEligibility(UUID athleteUserId,
                                                                          UUID actorId,
                                                                          UserRole actorRole) {
        // Athletes may only check their own eligibility
        if (actorRole == UserRole.ROLE_ATHLETE && !athleteUserId.equals(actorId)) {
            throw new MedicalAccessException("Athletes may only view their own eligibility");
        }

        // Get the most recent clearance by decisionDate
        Page<MedicalClearance> page = clearanceRepository.findLatestByAthleteUserId(
                athleteUserId,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "decisionDate")));

        if (page.isEmpty()) {
            return ParticipationEligibilityResponse.builder()
                    .athleteUserId(athleteUserId)
                    .eligible(false)
                    .note("No medical clearance on file")
                    .build();
        }

        MedicalClearance latest = page.getContent().getFirst();
        ClearanceStatus status = ClearanceStatus.valueOf(latest.getStatus());

        // Treat as expired if expiry date has passed
        boolean expiredByDate = latest.getExpiryDate() != null
                && latest.getExpiryDate().isBefore(LocalDate.now());

        if (status == ClearanceStatus.EXPIRED || status == ClearanceStatus.REVOKED
                || expiredByDate) {
            return ParticipationEligibilityResponse.builder()
                    .athleteUserId(athleteUserId)
                    .activityScope(latest.getActivityScope())
                    .eligible(false)
                    .status(expiredByDate ? ClearanceStatus.EXPIRED.name() : status.name())
                    .expiryDate(latest.getExpiryDate())
                    .note(expiredByDate
                            ? "Clearance has expired — reassessment required"
                            : "Clearance is " + status.name().toLowerCase().replace('_', ' ')
                              + " and is not valid")
                    .build();
        }

        boolean eligible = status == ClearanceStatus.CLEARED
                || status == ClearanceStatus.CLEARED_WITH_RESTRICTIONS;

        return ParticipationEligibilityResponse.builder()
                .athleteUserId(athleteUserId)
                .activityScope(latest.getActivityScope())
                .eligible(eligible)
                .restrictions(latest.getParticipationRestrictions())
                .requiredAccommodations(latest.getRequiredAccommodations())
                .expiryDate(latest.getExpiryDate())
                .status(status.name())
                .note(eligible ? null : "Athlete has not been cleared for participation")
                .build();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void requireClinicalWriteRole(UserRole role) {
        if (role != UserRole.ROLE_MEDICAL_OFFICER && role != UserRole.ROLE_ADMIN) {
            throw new MedicalAccessException(
                    "Only medical officers and admins may issue or revoke medical clearances");
        }
    }

    private MedicalClearance findOrThrow(UUID id) {
        return clearanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical clearance not found with id: " + id));
    }

    private MedicalClearanceResponse mapToResponse(MedicalClearance c) {
        return MedicalClearanceResponse.builder()
                .id(c.getId())
                .athleteUserId(c.getAthleteUserId())
                .issuedBy(c.getIssuedBy())
                .relatedAssessmentId(c.getRelatedAssessmentId())
                .relatedInjuryId(c.getRelatedInjuryId())
                .activityScope(c.getActivityScope())
                .status(c.getStatus())
                .decisionDate(c.getDecisionDate())
                .effectiveDate(c.getEffectiveDate())
                .expiryDate(c.getExpiryDate())
                .participationRestrictions(c.getParticipationRestrictions())
                .requiredAccommodations(c.getRequiredAccommodations())
                .followUpRequirements(c.getFollowUpRequirements())
                .conditionalReason(c.getConditionalReason())
                .revocationReason(c.getRevocationReason())
                .revokedBy(c.getRevokedBy())
                .revokedAt(c.getRevokedAt())
                .previousClearanceId(c.getPreviousClearanceId())
                .createdDate(c.getCreatedDate())
                .lastModifiedDate(c.getLastModifiedDate())
                .build();
    }

    /** Operational view only — strips all clinical / confidential fields. */
    private ParticipationEligibilityResponse mapToEligibility(MedicalClearance c) {
        ClearanceStatus status = ClearanceStatus.valueOf(c.getStatus());
        boolean expiredByDate = c.getExpiryDate() != null
                && c.getExpiryDate().isBefore(LocalDate.now());
        boolean eligible = (status == ClearanceStatus.CLEARED
                || status == ClearanceStatus.CLEARED_WITH_RESTRICTIONS)
                && !expiredByDate;

        return ParticipationEligibilityResponse.builder()
                .athleteUserId(c.getAthleteUserId())
                .activityScope(c.getActivityScope())
                .eligible(eligible)
                .restrictions(c.getParticipationRestrictions())
                .requiredAccommodations(c.getRequiredAccommodations())
                .expiryDate(c.getExpiryDate())
                .status(expiredByDate ? ClearanceStatus.EXPIRED.name() : c.getStatus())
                .note(eligible ? null : "Athlete is not currently cleared for this activity")
                .build();
    }
}
