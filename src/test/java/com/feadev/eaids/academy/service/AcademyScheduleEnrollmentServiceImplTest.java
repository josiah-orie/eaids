package com.feadev.eaids.academy.service;

import com.feadev.eaids.academy.dto.request.EnrollApplicantRequest;
import com.feadev.eaids.academy.dto.response.EnrollmentResponse;
import com.feadev.eaids.academy.event.ApplicantEnrolledEvent;
import com.feadev.eaids.academy.model.AcademyIndividualApplication;
import com.feadev.eaids.academy.model.AcademyTrainingSchedule;
import com.feadev.eaids.academy.model.enums.ApplicantType;
import com.feadev.eaids.academy.model.enums.ApplicationStatus;
import com.feadev.eaids.academy.model.enums.Location;
import com.feadev.eaids.academy.model.enums.ScheduleTargetType;
import com.feadev.eaids.academy.repository.AcademyIndividualApplicationRepository;
import com.feadev.eaids.academy.repository.AcademyInstitutionApplicationRepository;
import com.feadev.eaids.academy.repository.AcademyScheduleEnrollmentRepository;
import com.feadev.eaids.academy.repository.AcademyTrainingScheduleRepository;
import com.feadev.eaids.academy.service.impl.AcademyScheduleEnrollmentServiceImpl;
import com.feadev.eaids.registrations.exception.DuplicateResourceException;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.service.AuditService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademyScheduleEnrollmentServiceImplTest {

    @Mock private AcademyScheduleEnrollmentRepository enrollmentRepository;
    @Mock private AcademyTrainingScheduleRepository scheduleRepository;
    @Mock private AcademyIndividualApplicationRepository individualRepository;
    @Mock private AcademyInstitutionApplicationRepository institutionRepository;
    @Mock private AuditService auditService;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AcademyScheduleEnrollmentServiceImpl service;

    private AcademyTrainingSchedule schedule(ScheduleTargetType target, int capacity) {
        return AcademyTrainingSchedule.builder()
                .id(UUID.randomUUID())
                .title("U16 Camp")
                .targetType(target)
                .capacity(capacity)
                .startDate(LocalDate.now().plusDays(7))
                .build();
    }

    private AcademyIndividualApplication approvedIndividual() {
        return AcademyIndividualApplication.builder()
                .id(UUID.randomUUID())
                .fullName("Ada Obi")
                .email("ada@example.com")
                .location(Location.LAGOS)
                .status(ApplicationStatus.APPROVED)
                .build();
    }

    @Test
    @DisplayName("enroll approved individual publishes event and returns response")
    void enrollApprovedIndividual() {
        AcademyTrainingSchedule schedule = schedule(ScheduleTargetType.INDIVIDUAL, 40);
        AcademyIndividualApplication app = approvedIndividual();

        when(scheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));
        when(enrollmentRepository.countByScheduleId(schedule.getId())).thenReturn(0L);
        when(individualRepository.findById(app.getId())).thenReturn(Optional.of(app));
        when(enrollmentRepository.existsByScheduleIdAndIndividualApplicationId(schedule.getId(), app.getId()))
                .thenReturn(false);
        when(enrollmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EnrollmentResponse response = service.enroll(schedule.getId(),
                EnrollApplicantRequest.builder()
                        .applicantType(ApplicantType.INDIVIDUAL).applicationId(app.getId()).build());

        assertThat(response.getApplicantType()).isEqualTo("INDIVIDUAL");
        assertThat(response.getApplicantEmail()).isEqualTo("ada@example.com");
        verify(eventPublisher).publishEvent(any(ApplicantEnrolledEvent.class));
    }

    @Test
    @DisplayName("enroll rejects a non-approved applicant")
    void enrollRejectsNonApproved() {
        AcademyTrainingSchedule schedule = schedule(ScheduleTargetType.INDIVIDUAL, 40);
        AcademyIndividualApplication app = approvedIndividual();
        app.setStatus(ApplicationStatus.PENDING);

        when(scheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));
        when(enrollmentRepository.countByScheduleId(schedule.getId())).thenReturn(0L);
        when(individualRepository.findById(app.getId())).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> service.enroll(schedule.getId(),
                EnrollApplicantRequest.builder()
                        .applicantType(ApplicantType.INDIVIDUAL).applicationId(app.getId()).build()))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("APPROVED");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("enroll rejects an applicant type that does not match the schedule target")
    void enrollRejectsTypeMismatch() {
        AcademyTrainingSchedule schedule = schedule(ScheduleTargetType.INSTITUTION, 40);

        when(scheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));

        assertThatThrownBy(() -> service.enroll(schedule.getId(),
                EnrollApplicantRequest.builder()
                        .applicantType(ApplicantType.INDIVIDUAL).applicationId(UUID.randomUUID()).build()))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("target type");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("enroll rejects when schedule is at full capacity")
    void enrollRejectsAtCapacity() {
        AcademyTrainingSchedule schedule = schedule(ScheduleTargetType.INDIVIDUAL, 2);

        when(scheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));
        when(enrollmentRepository.countByScheduleId(schedule.getId())).thenReturn(2L);

        assertThatThrownBy(() -> service.enroll(schedule.getId(),
                EnrollApplicantRequest.builder()
                        .applicantType(ApplicantType.INDIVIDUAL).applicationId(UUID.randomUUID()).build()))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("capacity");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("enroll rejects a duplicate enrollment")
    void enrollRejectsDuplicate() {
        AcademyTrainingSchedule schedule = schedule(ScheduleTargetType.BOTH, 40);
        AcademyIndividualApplication app = approvedIndividual();

        when(scheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));
        when(enrollmentRepository.countByScheduleId(schedule.getId())).thenReturn(1L);
        when(individualRepository.findById(app.getId())).thenReturn(Optional.of(app));
        when(enrollmentRepository.existsByScheduleIdAndIndividualApplicationId(schedule.getId(), app.getId()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.enroll(schedule.getId(),
                EnrollApplicantRequest.builder()
                        .applicantType(ApplicantType.INDIVIDUAL).applicationId(app.getId()).build()))
                .isInstanceOf(DuplicateResourceException.class);

        verify(enrollmentRepository, never()).save(any());
    }
}
