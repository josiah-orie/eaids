package com.feadev.eaids.academy.event;

import com.feadev.eaids.academy.event.listener.AcademyNotificationListener;
import com.feadev.eaids.registrations.service.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AcademyNotificationListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AcademyNotificationListener listener;

    @Test
    @DisplayName("individual approved event triggers approval email")
    void individualApprovedSendsEmail() {
        listener.handleIndividualApproved(
                new IndividualApplicationApprovedEvent(UUID.randomUUID(), "ada@example.com", "Ada Obi"));
        verify(emailService).sendApplicationApprovedEmail("ada@example.com", "Ada Obi");
    }

    @Test
    @DisplayName("individual rejected event triggers rejection email with reason")
    void individualRejectedSendsEmail() {
        listener.handleIndividualRejected(new IndividualApplicationRejectedEvent(
                UUID.randomUUID(), "ada@example.com", "Ada Obi", "Outside catchment area"));
        verify(emailService).sendApplicationRejectedEmail("ada@example.com", "Ada Obi", "Outside catchment area");
    }

    @Test
    @DisplayName("institution approved event triggers approval email")
    void institutionApprovedSendsEmail() {
        listener.handleInstitutionApproved(new InstitutionApplicationApprovedEvent(
                UUID.randomUUID(), "sports@unilag.edu.ng", "University of Lagos"));
        verify(emailService).sendApplicationApprovedEmail("sports@unilag.edu.ng", "University of Lagos");
    }

    @Test
    @DisplayName("staff assigned event triggers assignment email")
    void staffAssignedSendsEmail() {
        listener.handleStaffAssigned(new com.feadev.eaids.academy.event.ScheduleStaffAssignedEvent(
                UUID.randomUUID(), "bello@eaids.com", "Coach Bello", "U16 Camp"));
        verify(emailService).sendStaffAssignmentEmail("bello@eaids.com", "Coach Bello", "U16 Camp");
    }

    @Test
    @DisplayName("applicant enrolled event triggers enrollment email")
    void applicantEnrolledSendsEmail() {
        listener.handleApplicantEnrolled(new com.feadev.eaids.academy.event.ApplicantEnrolledEvent(
                UUID.randomUUID(), "ada@example.com", "Ada Obi", "U16 Camp", "2026-09-05"));
        verify(emailService).sendEnrollmentEmail("ada@example.com", "Ada Obi", "U16 Camp", "2026-09-05");
    }
}
