package com.feadev.eaids.enrollment.service.impl;

import com.feadev.eaids.camp.model.Camp;
import com.feadev.eaids.camp.model.enums.CampStatus;
import com.feadev.eaids.camp.repository.CampRepository;
import com.feadev.eaids.enrollment.dto.response.EnrollmentResponse;
import com.feadev.eaids.enrollment.event.AthleteEnrolledEvent;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.model.enums.EnrollmentStatus;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.enrollment.service.EnrollmentService;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.UserStatus;
import com.feadev.eaids.registrations.repository.AthleteProfileRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    private final CampRepository campRepository;
    private final CampEnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final AthleteProfileRepository athleteProfileRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public EnrollmentResponse enrollAthlete(UUID campId, UUID athleteUserId) {
        Camp camp = campRepository.findById(campId)
                .orElseThrow(() -> new ResourceNotFoundException("Camp not found with id: " + campId));

        User user = userRepository.findById(athleteUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + athleteUserId));

        // Business rule: camp must be in REGISTRATION_OPEN status
        if (camp.getStatus() != CampStatus.REGISTRATION_OPEN) {
            throw new IllegalStateException("Camp is not open for registration. Current status: " + camp.getStatus());
        }

        // Business rule: registration deadline check
        if (camp.getRegistrationDeadline() != null && LocalDate.now().isAfter(camp.getRegistrationDeadline())) {
            throw new IllegalStateException("Registration deadline has passed");
        }

        // Business rule: no duplicate enrollments
        if (enrollmentRepository.existsByCampIdAndUserId(campId, athleteUserId)) {
            throw new IllegalStateException("Athlete is already enrolled in this camp");
        }

        // Business rule: athlete must be ACTIVE
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalStateException("Athlete account must be in ACTIVE status to enroll");
        }

        // Business rule: user must have an athlete profile
        if (!athleteProfileRepository.existsByUser_Id(athleteUserId)) {
            throw new IllegalStateException("User must have a completed athlete profile to enroll");
        }

        // Determine enrollment status based on capacity
        int confirmedCount = enrollmentRepository.countByCampIdAndStatusIn(campId,
                List.of(EnrollmentStatus.CONFIRMED, EnrollmentStatus.PENDING));
        EnrollmentStatus enrollmentStatus;
        if (confirmedCount >= camp.getCapacity()) {
            enrollmentStatus = EnrollmentStatus.WAITLISTED;
        } else {
            enrollmentStatus = EnrollmentStatus.CONFIRMED;
            camp.setCurrentEnrollment(camp.getCurrentEnrollment() + 1);
            campRepository.save(camp);
        }

        CampEnrollment enrollment = CampEnrollment.builder()
                .camp(camp)
                .user(user)
                .status(enrollmentStatus)
                .enrollmentNumber(confirmedCount + 1)
                .build();

        CampEnrollment saved = enrollmentRepository.save(enrollment);

        eventPublisher.publishEvent(new AthleteEnrolledEvent(
                saved.getId(), campId, athleteUserId, enrollmentStatus));

        log.info("Athlete {} enrolled in camp {} with status {}",
                athleteUserId, campId, enrollmentStatus);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getCampEnrollments(UUID campId) {
        if (!campRepository.existsById(campId)) {
            throw new ResourceNotFoundException("Camp not found with id: " + campId);
        }
        return enrollmentRepository.findByCampId(campId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getCampEnrollmentsByStatus(UUID campId, EnrollmentStatus status) {
        if (!campRepository.existsById(campId)) {
            throw new ResourceNotFoundException("Camp not found with id: " + campId);
        }
        return enrollmentRepository.findByCampIdAndStatus(campId, status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EnrollmentResponse updateEnrollmentStatus(UUID campId, UUID enrollmentId, EnrollmentStatus newStatus) {
        CampEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));

        if (!enrollment.getCamp().getId().equals(campId)) {
            throw new IllegalStateException("Enrollment does not belong to the specified camp");
        }

        EnrollmentStatus previousStatus = enrollment.getStatus();
        enrollment.setStatus(newStatus);
        CampEnrollment saved = enrollmentRepository.save(enrollment);

        // Update camp enrollment count
        Camp camp = enrollment.getCamp();
        if (previousStatus == EnrollmentStatus.CONFIRMED && newStatus != EnrollmentStatus.CONFIRMED) {
            camp.setCurrentEnrollment(Math.max(0, camp.getCurrentEnrollment() - 1));
            campRepository.save(camp);
        } else if (previousStatus != EnrollmentStatus.CONFIRMED && newStatus == EnrollmentStatus.CONFIRMED) {
            camp.setCurrentEnrollment(camp.getCurrentEnrollment() + 1);
            campRepository.save(camp);
        }

        log.info("Enrollment {} status changed from {} to {}", enrollmentId, previousStatus, newStatus);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getAthleteEnrollments(UUID athleteUserId) {
        return enrollmentRepository.findByUserId(athleteUserId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private EnrollmentResponse mapToResponse(CampEnrollment enrollment) {
        String athleteName = athleteProfileRepository.findByUser_Id(enrollment.getUser().getId())
                .map(profile -> profile.getFullName())
                .orElse("Unknown");

        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .campId(enrollment.getCamp().getId())
                .campName(enrollment.getCamp().getName())
                .userId(enrollment.getUser().getId())
                .athleteName(athleteName)
                .status(enrollment.getStatus().name())
                .enrollmentNumber(enrollment.getEnrollmentNumber())
                .createdDate(enrollment.getCreatedDate())
                .lastModifiedDate(enrollment.getLastModifiedDate())
                .build();
    }
}
