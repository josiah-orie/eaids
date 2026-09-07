package com.feadev.eaids.academy.event.listener;

import com.feadev.eaids.academy.event.ApplicantEnrolledEvent;
import com.feadev.eaids.academy.event.IndividualApplicationApprovedEvent;
import com.feadev.eaids.academy.event.IndividualApplicationRejectedEvent;
import com.feadev.eaids.academy.event.InstitutionApplicationApprovedEvent;
import com.feadev.eaids.academy.event.InstitutionApplicationRejectedEvent;
import com.feadev.eaids.academy.event.ScheduleStaffAssignedEvent;
import com.feadev.eaids.registrations.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends academy notification emails after the owning transaction commits.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AcademyNotificationListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void handleIndividualApproved(IndividualApplicationApprovedEvent event) {
        log.info("Processing IndividualApplicationApprovedEvent for {}", event.getEmail());
        emailService.sendApplicationApprovedEmail(event.getEmail(), event.getFullName());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void handleIndividualRejected(IndividualApplicationRejectedEvent event) {
        log.info("Processing IndividualApplicationRejectedEvent for {}", event.getEmail());
        emailService.sendApplicationRejectedEmail(event.getEmail(), event.getFullName(), event.getReason());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void handleInstitutionApproved(InstitutionApplicationApprovedEvent event) {
        log.info("Processing InstitutionApplicationApprovedEvent for {}", event.getContactEmail());
        emailService.sendApplicationApprovedEmail(event.getContactEmail(), event.getInstitutionName());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void handleInstitutionRejected(InstitutionApplicationRejectedEvent event) {
        log.info("Processing InstitutionApplicationRejectedEvent for {}", event.getContactEmail());
        emailService.sendApplicationRejectedEmail(event.getContactEmail(),
                event.getInstitutionName(), event.getReason());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void handleStaffAssigned(ScheduleStaffAssignedEvent event) {
        log.info("Processing ScheduleStaffAssignedEvent for {}", event.getStaffEmail());
        emailService.sendStaffAssignmentEmail(event.getStaffEmail(), event.getStaffName(),
                event.getScheduleTitle());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public void handleApplicantEnrolled(ApplicantEnrolledEvent event) {
        log.info("Processing ApplicantEnrolledEvent for {}", event.getApplicantEmail());
        emailService.sendEnrollmentEmail(event.getApplicantEmail(), event.getApplicantName(),
                event.getScheduleTitle(), event.getStartDate());
    }

    @Recover
    public void recover(Exception ex, Object event) {
        log.error("Permanent failure delivering academy notification for {} after retries: {}",
                event.getClass().getSimpleName(), ex.getMessage(), ex);
    }
}
