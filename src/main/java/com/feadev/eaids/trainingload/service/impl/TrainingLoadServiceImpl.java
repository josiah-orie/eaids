package com.feadev.eaids.trainingload.service.impl;

import com.feadev.eaids.audit.AuditInterceptor;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.trainingload.dto.request.CreateTrainingLoadRequest;
import com.feadev.eaids.trainingload.dto.response.TrainingLoadResponse;
import com.feadev.eaids.trainingload.model.TrainingLoadRecord;
import com.feadev.eaids.trainingload.repository.TrainingLoadRepository;
import com.feadev.eaids.trainingload.service.TrainingLoadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingLoadServiceImpl implements TrainingLoadService {

    private final TrainingLoadRepository trainingLoadRepository;
    private final CampEnrollmentRepository campEnrollmentRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final AuditInterceptor auditInterceptor;

    @Override
    @Transactional
    public TrainingLoadResponse recordTrainingLoad(UUID campId, UUID enrollmentId, CreateTrainingLoadRequest request) {
        // Validate enrollment exists and belongs to this camp
        CampEnrollment enrollment = campEnrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id: " + enrollmentId));

        if (!enrollment.getCamp().getId().equals(campId)) {
            throw new IllegalArgumentException("Enrollment does not belong to the specified camp");
        }

        // Validate session date is not in the future
        if (request.getSessionDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Session date cannot be in the future");
        }

        // Get current user
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        TrainingLoadRecord record = TrainingLoadRecord.builder()
                .enrollmentId(enrollmentId)
                .campId(campId)
                .sessionDate(request.getSessionDate())
                .durationMinutes(request.getDurationMinutes())
                .intensity(request.getIntensity())
                .sessionType(request.getSessionType())
                .recordedBy(currentUser.getId())
                .notes(request.getNotes())
                .build();

        TrainingLoadRecord saved = trainingLoadRepository.save(record);

        // Audit logging
        auditService.log(
                AuditAction.TRAINING_LOAD_RECORDED,
                currentUser.getId(),
                currentUserEmail,
                auditInterceptor.getClientIpAddress(),
                auditInterceptor.getUserAgent()
        );

        log.info("Training load recorded: enrollmentId={}, campId={}, date={}", enrollmentId, campId, request.getSessionDate());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrainingLoadResponse> getTrainingLoadHistory(UUID enrollmentId, Pageable pageable) {
        return trainingLoadRepository.findByEnrollmentIdOrderBySessionDateDesc(enrollmentId, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainingLoadResponse getTrainingLoadById(UUID id) {
        TrainingLoadRecord record = trainingLoadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Training load record not found with id: " + id));
        return mapToResponse(record);
    }

    @Override
    @Transactional
    public void deleteTrainingLoad(UUID id) {
        if (!trainingLoadRepository.existsById(id)) {
            throw new ResourceNotFoundException("Training load record not found with id: " + id);
        }
        trainingLoadRepository.deleteById(id);
    }

    private TrainingLoadResponse mapToResponse(TrainingLoadRecord record) {
        return TrainingLoadResponse.builder()
                .id(record.getId())
                .enrollmentId(record.getEnrollmentId())
                .campId(record.getCampId())
                .sessionDate(record.getSessionDate())
                .durationMinutes(record.getDurationMinutes())
                .intensity(record.getIntensity())
                .sessionType(record.getSessionType())
                .recordedBy(record.getRecordedBy())
                .notes(record.getNotes())
                .createdDate(record.getCreatedDate())
                .build();
    }
}
