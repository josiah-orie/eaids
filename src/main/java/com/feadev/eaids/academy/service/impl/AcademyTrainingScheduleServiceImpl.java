package com.feadev.eaids.academy.service.impl;

import com.feadev.eaids.academy.dto.request.CreateTrainingScheduleRequest;
import com.feadev.eaids.academy.dto.request.UpdateTrainingScheduleRequest;
import com.feadev.eaids.academy.dto.response.TrainingScheduleResponse;
import com.feadev.eaids.academy.model.AcademyTrainingSchedule;
import com.feadev.eaids.academy.model.enums.ScheduleStatus;
import com.feadev.eaids.academy.model.enums.ScheduleTargetType;
import com.feadev.eaids.academy.repository.AcademyTrainingScheduleRepository;
import com.feadev.eaids.academy.service.AcademyTrainingScheduleService;
import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.service.AuditService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AcademyTrainingScheduleServiceImpl implements AcademyTrainingScheduleService {

    private final AcademyTrainingScheduleRepository repository;
    private final AuditService auditService;

    @Override
    public TrainingScheduleResponse create(CreateTrainingScheduleRequest request) {
        validateDateRange(request.getStartDate(), request.getEndDate());

        AcademyTrainingSchedule schedule = AcademyTrainingSchedule.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .ageCategory(request.getAgeCategory())
                .targetType(request.getTargetType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .location(request.getLocation())
                .capacity(request.getCapacity())
                .status(ScheduleStatus.DRAFT)
                .build();

        AcademyTrainingSchedule saved = repository.save(schedule);
        audit(AuditAction.ACADEMY_SCHEDULE_CREATED);
        log.info("Academy training schedule created: id={}, title={}", saved.getId(), saved.getTitle());
        return AcademyMapper.toResponse(saved);
    }

    @Override
    public TrainingScheduleResponse update(UUID scheduleId, UpdateTrainingScheduleRequest request) {
        AcademyTrainingSchedule schedule = findOrThrow(scheduleId);

        if (schedule.getStatus() != ScheduleStatus.DRAFT) {
            throw new InvalidStateTransitionException(
                    "Only DRAFT schedules can be updated. Current status: " + schedule.getStatus());
        }
        validateDateRange(request.getStartDate(), request.getEndDate());

        schedule.setTitle(request.getTitle());
        schedule.setDescription(request.getDescription());
        schedule.setAgeCategory(request.getAgeCategory());
        schedule.setTargetType(request.getTargetType());
        schedule.setStartDate(request.getStartDate());
        schedule.setEndDate(request.getEndDate());
        schedule.setLocation(request.getLocation());
        schedule.setCapacity(request.getCapacity());

        AcademyTrainingSchedule saved = repository.save(schedule);
        audit(AuditAction.ACADEMY_SCHEDULE_UPDATED);
        log.info("Academy training schedule updated: id={}", saved.getId());
        return AcademyMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainingScheduleResponse get(UUID scheduleId) {
        return AcademyMapper.toResponse(findOrThrow(scheduleId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrainingScheduleResponse> list(String ageCategory, String targetType,
                                               String status, Pageable pageable) {
        Specification<AcademyTrainingSchedule> spec = buildSpecification(ageCategory, targetType, status);
        return repository.findAll(spec, pageable).map(AcademyMapper::toResponse);
    }

    @Override
    public TrainingScheduleResponse publish(UUID scheduleId) {
        AcademyTrainingSchedule schedule = findOrThrow(scheduleId);
        if (schedule.getStatus() != ScheduleStatus.DRAFT) {
            throw new InvalidStateTransitionException(
                    "Only DRAFT schedules can be published. Current status: " + schedule.getStatus());
        }
        schedule.setStatus(ScheduleStatus.PUBLISHED);
        AcademyTrainingSchedule saved = repository.save(schedule);
        audit(AuditAction.ACADEMY_SCHEDULE_STATUS_CHANGED);
        log.info("Academy training schedule published: id={}", saved.getId());
        return AcademyMapper.toResponse(saved);
    }

    @Override
    public TrainingScheduleResponse cancel(UUID scheduleId) {
        AcademyTrainingSchedule schedule = findOrThrow(scheduleId);
        if (schedule.getStatus() == ScheduleStatus.CANCELLED
                || schedule.getStatus() == ScheduleStatus.COMPLETED) {
            throw new InvalidStateTransitionException(
                    "Cannot cancel a schedule with status: " + schedule.getStatus());
        }
        schedule.setStatus(ScheduleStatus.CANCELLED);
        AcademyTrainingSchedule saved = repository.save(schedule);
        audit(AuditAction.ACADEMY_SCHEDULE_STATUS_CHANGED);
        log.info("Academy training schedule cancelled: id={}", saved.getId());
        return AcademyMapper.toResponse(saved);
    }

    private void validateDateRange(java.time.LocalDate start, java.time.LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new InvalidStateTransitionException("End date must not be before start date");
        }
    }

    private AcademyTrainingSchedule findOrThrow(UUID scheduleId) {
        return repository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Training schedule not found: " + scheduleId));
    }

    private Specification<AcademyTrainingSchedule> buildSpecification(String ageCategory,
                                                                      String targetType, String status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (ageCategory != null && !ageCategory.isBlank()) {
                predicates.add(cb.equal(root.get("ageCategory"),
                        AgeCategory.valueOf(ageCategory.toUpperCase())));
            }
            if (targetType != null && !targetType.isBlank()) {
                predicates.add(cb.equal(root.get("targetType"),
                        ScheduleTargetType.valueOf(targetType.toUpperCase())));
            }
            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(root.get("status"),
                        ScheduleStatus.valueOf(status.toUpperCase())));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void audit(AuditAction action) {
        String email = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName() : null;
        auditService.log(action, null, email, null, null);
    }
}
