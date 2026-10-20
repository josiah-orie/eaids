package com.feadev.eaids.medical.service.impl;

import com.feadev.eaids.audit.AuditInterceptor;
import com.feadev.eaids.medical.dto.request.ReportInjuryRequest;
import com.feadev.eaids.medical.dto.request.UpdateInjuryRequest;
import com.feadev.eaids.medical.dto.response.InjuryResponse;
import com.feadev.eaids.medical.dto.response.InjurySummaryResponse;
import com.feadev.eaids.medical.model.AthleteInjury;
import com.feadev.eaids.medical.model.enums.InjuryStatus;
import com.feadev.eaids.medical.repository.AthleteInjuryRepository;
import com.feadev.eaids.medical.service.AthleteInjuryService;
import com.feadev.eaids.medical.service.InjuryRiskService;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.exception.MedicalAccessException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.model.enums.UserRole;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.registrations.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AthleteInjuryServiceImpl implements AthleteInjuryService {

    private final AthleteInjuryRepository injuryRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final AuditInterceptor auditInterceptor;
    private final EmailService emailService;
    private final InjuryRiskService injuryRiskService;

    private static final Map<InjuryStatus, Set<InjuryStatus>> VALID_TRANSITIONS = Map.of(
            InjuryStatus.REPORTED,             Set.of(InjuryStatus.AWAITING_ASSESSMENT),
            InjuryStatus.AWAITING_ASSESSMENT,  Set.of(InjuryStatus.UNDER_TREATMENT),
            InjuryStatus.UNDER_TREATMENT,      Set.of(InjuryStatus.REHABILITATION),
            InjuryStatus.REHABILITATION,       Set.of(InjuryStatus.AWAITING_CLEARANCE),
            InjuryStatus.AWAITING_CLEARANCE,   Set.of(InjuryStatus.RESOLVED),
            InjuryStatus.RESOLVED,             Set.of(InjuryStatus.CLOSED),
            InjuryStatus.CLOSED,               Set.of()
    );

    @Override
    public Object reportInjury(UUID athleteUserId, ReportInjuryRequest request,
                                UUID reportedBy, UserRole actorRole) {
        AthleteInjury injury = AthleteInjury.builder()
                .athleteUserId(athleteUserId)
                .campId(request.getCampId())
                .incidentDate(request.getIncidentDate())
                .reportedDate(request.getReportedDate())
                .incidentLocation(request.getIncidentLocation())
                .injuryContext(request.getInjuryContext())
                .bodyRegion(request.getBodyRegion())
                .injuryCategory(request.getInjuryCategory())
                .isSuspectedConcussion(request.isSuspectedConcussion())
                .mechanismOfInjury(request.getMechanismOfInjury())
                .symptomsReported(request.getSymptomsReported())
                .initialResponse(request.getInitialResponse())
                .status(InjuryStatus.REPORTED.name())
                .reportedBy(reportedBy)
                .build();

        AthleteInjury saved = injuryRepository.save(injury);

        auditService.log(AuditAction.MEDICAL_INJURY_REPORTED, reportedBy, reportedBy.toString(),
                auditInterceptor.getClientIpAddress(), auditInterceptor.getUserAgent());

        if (request.isSuspectedConcussion()) {
            sendConcussionAlerts(athleteUserId, reportedBy);
        }

        log.info("Injury reported: id={} athlete={} concussion={}", saved.getId(),
                athleteUserId, request.isSuspectedConcussion());

        // Evict cached risk score so next read reflects this new injury
        injuryRiskService.evictRiskCache(athleteUserId);

        return isFullAccessRole(actorRole) ? mapToFull(saved) : mapToSummary(saved);
    }

    @Override
    public Object updateInjury(UUID injuryId, UpdateInjuryRequest request,
                                UUID actorId, UserRole actorRole) {
        AthleteInjury injury = findOrThrow(injuryId);

        // Public fields — any authenticated user with a linked role may update
        if (request.getIncidentDate() != null)       injury.setIncidentDate(request.getIncidentDate());
        if (request.getIncidentLocation() != null)   injury.setIncidentLocation(request.getIncidentLocation());
        if (request.getInjuryContext() != null)      injury.setInjuryContext(request.getInjuryContext());
        if (request.getBodyRegion() != null)         injury.setBodyRegion(request.getBodyRegion());
        if (request.getInjuryCategory() != null)     injury.setInjuryCategory(request.getInjuryCategory());
        if (request.getMechanismOfInjury() != null)  injury.setMechanismOfInjury(request.getMechanismOfInjury());
        if (request.getSymptomsReported() != null)   injury.setSymptomsReported(request.getSymptomsReported());
        if (request.getInitialResponse() != null)    injury.setInitialResponse(request.getInitialResponse());

        // Clinician-only fields
        boolean hasClinicalFields = request.getSeverity() != null
                || request.getConfirmedDiagnosis() != null
                || request.getReferralDetails() != null
                || request.getTreatmentPlan() != null
                || request.getParticipationRestrictions() != null
                || request.getRehabilitationStatus() != null
                || request.getReturnToSportDecision() != null;

        if (hasClinicalFields && actorRole != UserRole.ROLE_MEDICAL_OFFICER
                && actorRole != UserRole.ROLE_ADMIN
                && actorRole != UserRole.ROLE_PHYSIOTHERAPIST) {
            throw new MedicalAccessException(
                    "Only medical officers, physiotherapists, and admins may update clinical injury fields");
        }

        if (request.getSeverity() != null)              injury.setSeverity(request.getSeverity());
        if (request.getConfirmedDiagnosis() != null)    injury.setConfirmedDiagnosis(request.getConfirmedDiagnosis());
        if (request.getReferralDetails() != null)       injury.setReferralDetails(request.getReferralDetails());
        if (request.getTreatmentPlan() != null)         injury.setTreatmentPlan(request.getTreatmentPlan());
        if (request.getParticipationRestrictions() != null) injury.setParticipationRestrictions(request.getParticipationRestrictions());
        if (request.getRehabilitationStatus() != null)  injury.setRehabilitationStatus(request.getRehabilitationStatus());
        if (request.getReturnToSportDecision() != null) injury.setReturnToSportDecision(request.getReturnToSportDecision());

        injury.setRecordedBy(actorId);
        AthleteInjury saved = injuryRepository.save(injury);
        // Evict cached risk score — injury data changed
        injuryRiskService.evictRiskCache(injury.getAthleteUserId());
        return isFullAccessRole(actorRole) ? mapToFull(saved) : mapToSummary(saved);
    }

    @Override
    public Object transitionStatus(UUID injuryId, InjuryStatus newStatus,
                                    UUID actorId, UserRole actorRole) {
        if (actorRole != UserRole.ROLE_MEDICAL_OFFICER
                && actorRole != UserRole.ROLE_ADMIN
                && actorRole != UserRole.ROLE_PHYSIOTHERAPIST) {
            throw new MedicalAccessException(
                    "Only medical officers, physiotherapists, and admins may transition injury status");
        }

        // RESOLVED and CLOSED require MO or admin
        if ((newStatus == InjuryStatus.RESOLVED || newStatus == InjuryStatus.CLOSED)
                && actorRole == UserRole.ROLE_PHYSIOTHERAPIST) {
            throw new MedicalAccessException(
                    "RESOLVED and CLOSED transitions require a medical officer or admin");
        }

        AthleteInjury injury = findOrThrow(injuryId);
        InjuryStatus current = InjuryStatus.valueOf(injury.getStatus());
        Set<InjuryStatus> allowed = VALID_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidStateTransitionException(
                    String.format("Cannot transition injury from %s to %s. Allowed: %s",
                            current, newStatus, allowed));
        }

        injury.setStatus(newStatus.name());
        injury.setRecordedBy(actorId);
        AthleteInjury saved = injuryRepository.save(injury);
        // Evict cached risk score — status change affects risk calculations
        injuryRiskService.evictRiskCache(injury.getAthleteUserId());
        return mapToFull(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Object getInjury(UUID injuryId, UUID actorId, UserRole actorRole) {
        AthleteInjury injury = findOrThrow(injuryId);
        return switch (actorRole) {
            case ROLE_MEDICAL_OFFICER, ROLE_ADMIN, ROLE_PHYSIOTHERAPIST -> mapToFull(injury);
            case ROLE_ATHLETE -> {
                if (!injury.getAthleteUserId().equals(actorId)) {
                    throw new MedicalAccessException("Athletes may only view their own injuries");
                }
                yield mapToSummary(injury);
            }
            case ROLE_COACH -> mapToSummary(injury); // coach can see public fields only
            default -> throw new MedicalAccessException(
                    "Your role does not permit access to injury records");
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Page<?> getInjuriesForAthlete(UUID athleteUserId, UUID actorId,
                                          UserRole actorRole, Pageable pageable) {
        if (actorRole == UserRole.ROLE_ATHLETE && !athleteUserId.equals(actorId)) {
            throw new MedicalAccessException("Athletes may only view their own injuries");
        }
        if (actorRole == UserRole.ROLE_SCOUT) {
            throw new MedicalAccessException("Scouts do not have access to injury records");
        }
        Page<AthleteInjury> page = injuryRepository.findByAthleteUserId(athleteUserId, pageable);
        return isFullAccessRole(actorRole)
                ? page.map(this::mapToFull)
                : page.map(this::mapToSummary);
    }

    // -----------------------------------------------------------------------
    // Concussion alert
    // -----------------------------------------------------------------------

    private void sendConcussionAlerts(UUID athleteUserId, UUID reportedBy) {
        // Retrieve athlete email for the alert (identifier only — no clinical detail)
        String athleteEmail = userRepository.findById(athleteUserId)
                .map(User::getEmail).orElse("unknown");
        String reporterEmail = userRepository.findById(reportedBy)
                .map(User::getEmail).orElse("unknown");

        // Notify all active admins and medical officers
        Stream.concat(
                userRepository.findByRole(UserRole.ROLE_ADMIN).stream(),
                userRepository.findByRole(UserRole.ROLE_MEDICAL_OFFICER).stream()
        ).distinct().forEach(u -> emailService.sendConcussionAlertEmail(
                u.getEmail(), athleteEmail, reporterEmail));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private boolean isFullAccessRole(UserRole role) {
        return role == UserRole.ROLE_MEDICAL_OFFICER
                || role == UserRole.ROLE_ADMIN
                || role == UserRole.ROLE_PHYSIOTHERAPIST;
    }

    private AthleteInjury findOrThrow(UUID id) {
        return injuryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Injury record not found with id: " + id));
    }

    private InjuryResponse mapToFull(AthleteInjury i) {
        return InjuryResponse.builder()
                .id(i.getId())
                .athleteUserId(i.getAthleteUserId())
                .campId(i.getCampId())
                .incidentDate(i.getIncidentDate())
                .reportedDate(i.getReportedDate())
                .incidentLocation(i.getIncidentLocation())
                .injuryContext(i.getInjuryContext())
                .bodyRegion(i.getBodyRegion())
                .injuryCategory(i.getInjuryCategory())
                .isSuspectedConcussion(i.isSuspectedConcussion())
                .mechanismOfInjury(i.getMechanismOfInjury())
                .symptomsReported(i.getSymptomsReported())
                .severity(i.getSeverity())
                .confirmedDiagnosis(i.getConfirmedDiagnosis())
                .initialResponse(i.getInitialResponse())
                .referralDetails(i.getReferralDetails())
                .treatmentPlan(i.getTreatmentPlan())
                .participationRestrictions(i.getParticipationRestrictions())
                .rehabilitationStatus(i.getRehabilitationStatus())
                .returnToSportDecision(i.getReturnToSportDecision())
                .status(i.getStatus())
                .reportedBy(i.getReportedBy())
                .recordedBy(i.getRecordedBy())
                .createdDate(i.getCreatedDate())
                .lastModifiedDate(i.getLastModifiedDate())
                .build();
    }

    private InjurySummaryResponse mapToSummary(AthleteInjury i) {
        return InjurySummaryResponse.builder()
                .id(i.getId())
                .athleteUserId(i.getAthleteUserId())
                .campId(i.getCampId())
                .incidentDate(i.getIncidentDate())
                .reportedDate(i.getReportedDate())
                .incidentLocation(i.getIncidentLocation())
                .injuryContext(i.getInjuryContext())
                .bodyRegion(i.getBodyRegion())
                .injuryCategory(i.getInjuryCategory())
                .isSuspectedConcussion(i.isSuspectedConcussion())
                .mechanismOfInjury(i.getMechanismOfInjury())
                .symptomsReported(i.getSymptomsReported())
                .initialResponse(i.getInitialResponse())
                .participationRestrictions(i.getParticipationRestrictions())
                .rehabilitationStatus(i.getRehabilitationStatus())
                .status(i.getStatus())
                .createdDate(i.getCreatedDate())
                .build();
    }
}
