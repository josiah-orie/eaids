package com.feadev.eaids.academy.service.impl;

import com.feadev.eaids.academy.dto.request.EnrollApplicantRequest;
import com.feadev.eaids.academy.dto.response.EnrollmentResponse;
import com.feadev.eaids.academy.event.ApplicantEnrolledEvent;
import com.feadev.eaids.academy.model.AcademyIndividualApplication;
import com.feadev.eaids.academy.model.AcademyInstitutionApplication;
import com.feadev.eaids.academy.model.AcademyScheduleEnrollment;
import com.feadev.eaids.academy.model.AcademyTrainingSchedule;
import com.feadev.eaids.academy.model.enums.ApplicantType;
import com.feadev.eaids.academy.model.enums.ApplicationStatus;
import com.feadev.eaids.academy.model.enums.ScheduleTargetType;
import com.feadev.eaids.academy.repository.AcademyIndividualApplicationRepository;
import com.feadev.eaids.academy.repository.AcademyInstitutionApplicationRepository;
import com.feadev.eaids.academy.repository.AcademyScheduleEnrollmentRepository;
import com.feadev.eaids.academy.repository.AcademyTrainingScheduleRepository;
import com.feadev.eaids.academy.service.AcademyScheduleEnrollmentService;
import com.feadev.eaids.registrations.exception.DuplicateResourceException;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AcademyScheduleEnrollmentServiceImpl implements AcademyScheduleEnrollmentService {

    private final AcademyScheduleEnrollmentRepository enrollmentRepository;
    private final AcademyTrainingScheduleRepository scheduleRepository;
    private final AcademyIndividualApplicationRepository individualRepository;
    private final AcademyInstitutionApplicationRepository institutionRepository;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public EnrollmentResponse enroll(UUID scheduleId, EnrollApplicantRequest request) {
        AcademyTrainingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Training schedule not found: " + scheduleId));

        validateTargetType(schedule.getTargetType(), request.getApplicantType());
        enforceCapacity(schedule, scheduleId);

        String email;
        String name;
        AcademyScheduleEnrollment.AcademyScheduleEnrollmentBuilder builder = AcademyScheduleEnrollment.builder()
                .schedule(schedule)
                .applicantType(request.getApplicantType());

        if (request.getApplicantType() == ApplicantType.INDIVIDUAL) {
            AcademyIndividualApplication app = individualRepository.findById(request.getApplicationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Individual application not found: " + request.getApplicationId()));
            requireApproved(app.getStatus());
            if (enrollmentRepository.existsByScheduleIdAndIndividualApplicationId(scheduleId, app.getId())) {
                throw new DuplicateResourceException(
                        "This applicant is already enrolled in the schedule");
            }
            builder.individualApplicationId(app.getId());
            email = app.getEmail();
            name = app.getFullName();
        } else {
            AcademyInstitutionApplication app = institutionRepository.findById(request.getApplicationId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Institution application not found: " + request.getApplicationId()));
            requireApproved(app.getStatus());
            if (enrollmentRepository.existsByScheduleIdAndInstitutionApplicationId(scheduleId, app.getId())) {
                throw new DuplicateResourceException(
                        "This institution is already enrolled in the schedule");
            }
            builder.institutionApplicationId(app.getId());
            email = app.getContactEmail();
            name = app.getName();
        }

        AcademyScheduleEnrollment saved = enrollmentRepository.save(builder.build());

        eventPublisher.publishEvent(new ApplicantEnrolledEvent(scheduleId, email, name,
                schedule.getTitle(),
                schedule.getStartDate() != null ? schedule.getStartDate().toString() : null));
        audit(AuditAction.ACADEMY_APPLICANT_ENROLLED);
        log.info("Academy applicant enrolled: scheduleId={}, applicantType={}, applicationId={}",
                scheduleId, request.getApplicantType(), request.getApplicationId());

        return toResponse(saved, name, email);
    }

    @Override
    public void unenroll(UUID scheduleId, UUID enrollmentId) {
        AcademyScheduleEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Enrollment not found: " + enrollmentId));
        if (enrollment.getSchedule() == null || !enrollment.getSchedule().getId().equals(scheduleId)) {
            throw new ResourceNotFoundException(
                    "Enrollment " + enrollmentId + " does not belong to schedule " + scheduleId);
        }
        enrollmentRepository.delete(enrollment);
        audit(AuditAction.ACADEMY_APPLICANT_UNENROLLED);
        log.info("Academy applicant unenrolled: scheduleId={}, enrollmentId={}", scheduleId, enrollmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> listEnrollments(UUID scheduleId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new ResourceNotFoundException("Training schedule not found: " + scheduleId);
        }
        return enrollmentRepository.findByScheduleId(scheduleId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateTargetType(ScheduleTargetType targetType, ApplicantType applicantType) {
        if (targetType == ScheduleTargetType.BOTH) {
            return;
        }
        boolean matches = (targetType == ScheduleTargetType.INDIVIDUAL
                && applicantType == ApplicantType.INDIVIDUAL)
                || (targetType == ScheduleTargetType.INSTITUTION
                && applicantType == ApplicantType.INSTITUTION);
        if (!matches) {
            throw new InvalidStateTransitionException(
                    "Applicant type " + applicantType + " does not match schedule target type " + targetType);
        }
    }

    private void enforceCapacity(AcademyTrainingSchedule schedule, UUID scheduleId) {
        long current = enrollmentRepository.countByScheduleId(scheduleId);
        if (current >= schedule.getCapacity()) {
            throw new InvalidStateTransitionException(
                    "Schedule is at full capacity (" + schedule.getCapacity() + ")");
        }
    }

    private void requireApproved(ApplicationStatus status) {
        if (status != ApplicationStatus.APPROVED) {
            throw new InvalidStateTransitionException(
                    "Only APPROVED applications can be enrolled. Current status: " + status);
        }
    }

    private EnrollmentResponse toResponse(AcademyScheduleEnrollment enrollment) {
        String name = null;
        String email = null;
        if (enrollment.getApplicantType() == ApplicantType.INDIVIDUAL
                && enrollment.getIndividualApplicationId() != null) {
            AcademyIndividualApplication app = individualRepository
                    .findById(enrollment.getIndividualApplicationId()).orElse(null);
            if (app != null) {
                name = app.getFullName();
                email = app.getEmail();
            }
        } else if (enrollment.getApplicantType() == ApplicantType.INSTITUTION
                && enrollment.getInstitutionApplicationId() != null) {
            AcademyInstitutionApplication app = institutionRepository
                    .findById(enrollment.getInstitutionApplicationId()).orElse(null);
            if (app != null) {
                name = app.getName();
                email = app.getContactEmail();
            }
        }
        return toResponse(enrollment, name, email);
    }

    private EnrollmentResponse toResponse(AcademyScheduleEnrollment enrollment, String name, String email) {
        UUID applicationId = enrollment.getApplicantType() == ApplicantType.INDIVIDUAL
                ? enrollment.getIndividualApplicationId() : enrollment.getInstitutionApplicationId();
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .scheduleId(enrollment.getSchedule() != null ? enrollment.getSchedule().getId() : null)
                .applicantType(enrollment.getApplicantType() != null
                        ? enrollment.getApplicantType().name() : null)
                .applicationId(applicationId)
                .applicantName(name)
                .applicantEmail(email)
                .enrolledDate(enrollment.getEnrolledDate())
                .build();
    }

    private void audit(AuditAction action) {
        String email = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName() : null;
        auditService.log(action, null, email, null, null);
    }
}
