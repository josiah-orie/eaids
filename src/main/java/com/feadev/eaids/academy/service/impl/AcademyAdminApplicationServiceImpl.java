package com.feadev.eaids.academy.service.impl;

import com.feadev.eaids.academy.dto.request.ReviewApplicationRequest;
import com.feadev.eaids.academy.dto.response.IndividualApplicationResponse;
import com.feadev.eaids.academy.dto.response.InstitutionApplicationResponse;
import com.feadev.eaids.academy.event.IndividualApplicationApprovedEvent;
import com.feadev.eaids.academy.event.IndividualApplicationRejectedEvent;
import com.feadev.eaids.academy.event.InstitutionApplicationApprovedEvent;
import com.feadev.eaids.academy.event.InstitutionApplicationRejectedEvent;
import com.feadev.eaids.academy.model.AcademyIndividualApplication;
import com.feadev.eaids.academy.model.AcademyInstitutionApplication;
import com.feadev.eaids.academy.model.enums.ApplicationStatus;
import com.feadev.eaids.academy.repository.AcademyIndividualApplicationRepository;
import com.feadev.eaids.academy.repository.AcademyInstitutionApplicationRepository;
import com.feadev.eaids.academy.service.AcademyAdminApplicationService;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AcademyAdminApplicationServiceImpl implements AcademyAdminApplicationService {

    private final AcademyIndividualApplicationRepository individualRepository;
    private final AcademyInstitutionApplicationRepository institutionRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(readOnly = true)
    public Page<IndividualApplicationResponse> listIndividuals(String status, Pageable pageable) {
        ApplicationStatus filter = parseStatus(status);
        Page<AcademyIndividualApplication> page = (filter == null)
                ? individualRepository.findAll(pageable)
                : individualRepository.findByStatus(filter, pageable);
        return page.map(AcademyMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InstitutionApplicationResponse> listInstitutions(String status, Pageable pageable) {
        ApplicationStatus filter = parseStatus(status);
        Page<AcademyInstitutionApplication> page = (filter == null)
                ? institutionRepository.findAll(pageable)
                : institutionRepository.findByStatus(filter, pageable);
        return page.map(AcademyMapper::toResponse);
    }

    @Override
    public IndividualApplicationResponse reviewIndividual(UUID applicationId,
                                                          ReviewApplicationRequest request) {
        AcademyIndividualApplication application = individualRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Individual application not found: " + applicationId));

        ApplicationStatus decision = validateDecision(request, application.getStatus());

        UUID adminId = currentUserId();
        application.setStatus(decision);
        application.setReviewNotes(request.getReviewNotes());
        application.setReviewedBy(adminId);
        application.setReviewDate(LocalDateTime.now());
        AcademyIndividualApplication saved = individualRepository.save(application);

        if (decision == ApplicationStatus.APPROVED) {
            eventPublisher.publishEvent(new IndividualApplicationApprovedEvent(
                    saved.getId(), saved.getEmail(), saved.getFullName()));
            auditService.log(AuditAction.ACADEMY_APPLICATION_APPROVED, adminId,
                    currentUserEmail(), null, null);
        } else {
            eventPublisher.publishEvent(new IndividualApplicationRejectedEvent(
                    saved.getId(), saved.getEmail(), saved.getFullName(), saved.getReviewNotes()));
            auditService.log(AuditAction.ACADEMY_APPLICATION_REJECTED, adminId,
                    currentUserEmail(), null, null);
        }

        log.info("Individual application reviewed: id={}, decision={}", applicationId, decision);
        return AcademyMapper.toResponse(saved);
    }

    @Override
    public InstitutionApplicationResponse reviewInstitution(UUID applicationId,
                                                            ReviewApplicationRequest request) {
        AcademyInstitutionApplication application = institutionRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Institution application not found: " + applicationId));

        ApplicationStatus decision = validateDecision(request, application.getStatus());

        UUID adminId = currentUserId();
        application.setStatus(decision);
        application.setReviewNotes(request.getReviewNotes());
        application.setReviewedBy(adminId);
        application.setReviewDate(LocalDateTime.now());
        AcademyInstitutionApplication saved = institutionRepository.save(application);

        if (decision == ApplicationStatus.APPROVED) {
            eventPublisher.publishEvent(new InstitutionApplicationApprovedEvent(
                    saved.getId(), saved.getContactEmail(), saved.getName()));
            auditService.log(AuditAction.ACADEMY_APPLICATION_APPROVED, adminId,
                    currentUserEmail(), null, null);
        } else {
            eventPublisher.publishEvent(new InstitutionApplicationRejectedEvent(
                    saved.getId(), saved.getContactEmail(), saved.getName(), saved.getReviewNotes()));
            auditService.log(AuditAction.ACADEMY_APPLICATION_REJECTED, adminId,
                    currentUserEmail(), null, null);
        }

        log.info("Institution application reviewed: id={}, decision={}", applicationId, decision);
        return AcademyMapper.toResponse(saved);
    }

    /**
     * Validates the review decision against the current application state and returns
     * the target status. Only PENDING applications may be reviewed, the decision must be
     * APPROVED or REJECTED, and a reason is mandatory for rejection.
     */
    private ApplicationStatus validateDecision(ReviewApplicationRequest request,
                                               ApplicationStatus currentStatus) {
        if (currentStatus != ApplicationStatus.PENDING) {
            throw new InvalidStateTransitionException(
                    "Only PENDING applications can be reviewed. Current status: " + currentStatus);
        }

        ApplicationStatus decision = request.getDecision();
        if (decision != ApplicationStatus.APPROVED && decision != ApplicationStatus.REJECTED) {
            throw new InvalidStateTransitionException(
                    "Review decision must be APPROVED or REJECTED");
        }

        if (decision == ApplicationStatus.REJECTED
                && (request.getReviewNotes() == null || request.getReviewNotes().isBlank())) {
            throw new InvalidStateTransitionException(
                    "A rejection reason (reviewNotes) is required when rejecting an application");
        }

        return decision;
    }

    private ApplicationStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        return ApplicationStatus.valueOf(status.toUpperCase());
    }

    private String currentUserEmail() {
       String userid = Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getName();
       UUID userId = UUID.fromString(userid);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Current admin info not found: " + userId));
        return user.getEmail();
    }

    private UUID currentUserId() {
        String email = currentUserEmail();
        log.info("Current admin user: {}", email);
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Current admin user not found: " + email));
    }
}
