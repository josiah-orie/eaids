package com.feadev.eaids.scouting.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.feadev.eaids.audit.AuditInterceptor;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.scouting.dto.request.CreateInterviewRequest;
import com.feadev.eaids.scouting.dto.request.ReviewInterviewRequest;
import com.feadev.eaids.scouting.dto.response.InterviewRequestResponse;
import com.feadev.eaids.scouting.event.InterviewRequestApprovedEvent;
import com.feadev.eaids.scouting.event.InterviewRequestRejectedEvent;
import com.feadev.eaids.scouting.event.InterviewRequestSubmittedEvent;
import com.feadev.eaids.scouting.model.InterviewRequest;
import com.feadev.eaids.scouting.model.enums.InterviewStatus;
import com.feadev.eaids.scouting.repository.InterviewRequestRepository;
import com.feadev.eaids.scouting.service.InterviewRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewRequestServiceImpl implements InterviewRequestService {

    private final InterviewRequestRepository interviewRequestRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;
    private final AuditInterceptor auditInterceptor;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public InterviewRequestResponse submitRequest(UUID scoutUserId, CreateInterviewRequest request) {
        String preferredDatesJson = null;
        if (request.getPreferredDates() != null) {
            try {
                preferredDatesJson = objectMapper.writeValueAsString(request.getPreferredDates());
            } catch (JsonProcessingException e) {
                preferredDatesJson = "[]";
            }
        }

        InterviewRequest entity = InterviewRequest.builder()
                .scoutUserId(scoutUserId)
                .athleteUserId(request.getAthleteUserId())
                .preferredDates(preferredDatesJson)
                .notes(request.getNotes())
                .status(InterviewStatus.PENDING)
                .build();

        InterviewRequest saved = interviewRequestRepository.save(entity);

        // Publish event
        eventPublisher.publishEvent(new InterviewRequestSubmittedEvent(
                saved.getId(), scoutUserId, request.getAthleteUserId()));

        // Audit
        String actor = SecurityContextHolder.getContext().getAuthentication().getName();
        auditService.log(AuditAction.INTERVIEW_REQUESTED, scoutUserId, actor,
                auditInterceptor.getClientIpAddress(), auditInterceptor.getUserAgent());

        log.info("Interview request submitted: id={}, scout={}, athlete={}",
                saved.getId(), scoutUserId, request.getAthleteUserId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterviewRequestResponse> getRequestsByStatus(InterviewStatus status, Pageable pageable) {
        return interviewRequestRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public InterviewRequestResponse reviewRequest(UUID requestId, ReviewInterviewRequest review) {
        InterviewRequest entity = interviewRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interview request not found: " + requestId));

        if (entity.getStatus() != InterviewStatus.PENDING) {
            throw new IllegalArgumentException("Can only review PENDING requests. Current status: " + entity.getStatus());
        }

        if (review.getStatus() != InterviewStatus.APPROVED && review.getStatus() != InterviewStatus.REJECTED) {
            throw new IllegalArgumentException("Review status must be APPROVED or REJECTED");
        }

        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        entity.setStatus(review.getStatus());
        entity.setReviewedBy(currentUser.getId());
        entity.setReviewNotes(review.getReviewNotes());
        entity.setReviewDate(LocalDateTime.now());

        InterviewRequest saved = interviewRequestRepository.save(entity);

        // Publish appropriate event
        if (review.getStatus() == InterviewStatus.APPROVED) {
            eventPublisher.publishEvent(new InterviewRequestApprovedEvent(
                    saved.getId(), saved.getScoutUserId(), saved.getAthleteUserId()));
            auditService.log(AuditAction.INTERVIEW_APPROVED, currentUser.getId(), currentUserEmail,
                    auditInterceptor.getClientIpAddress(), auditInterceptor.getUserAgent());
        } else {
            eventPublisher.publishEvent(new InterviewRequestRejectedEvent(
                    saved.getId(), saved.getScoutUserId(), saved.getAthleteUserId()));
            auditService.log(AuditAction.INTERVIEW_REJECTED, currentUser.getId(), currentUserEmail,
                    auditInterceptor.getClientIpAddress(), auditInterceptor.getUserAgent());
        }

        log.info("Interview request reviewed: id={}, newStatus={}", requestId, review.getStatus());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InterviewRequestResponse> getScoutRequests(UUID scoutUserId, Pageable pageable) {
        return interviewRequestRepository.findByScoutUserId(scoutUserId, pageable)
                .map(this::mapToResponse);
    }

    private InterviewRequestResponse mapToResponse(InterviewRequest entity) {
        List<String> dates = null;
        if (entity.getPreferredDates() != null) {
            try {
                dates = objectMapper.readValue(entity.getPreferredDates(), new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                dates = List.of();
            }
        }

        return InterviewRequestResponse.builder()
                .id(entity.getId())
                .scoutUserId(entity.getScoutUserId())
                .athleteUserId(entity.getAthleteUserId())
                .preferredDates(dates)
                .notes(entity.getNotes())
                .status(entity.getStatus())
                .reviewedBy(entity.getReviewedBy())
                .reviewNotes(entity.getReviewNotes())
                .reviewDate(entity.getReviewDate())
                .createdDate(entity.getCreatedDate())
                .build();
    }
}
