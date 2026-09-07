package com.feadev.eaids.academy.service.impl;

import com.feadev.eaids.academy.dto.request.AssignStaffRequest;
import com.feadev.eaids.academy.dto.response.ScheduleStaffResponse;
import com.feadev.eaids.academy.event.ScheduleStaffAssignedEvent;
import com.feadev.eaids.academy.model.AcademyScheduleStaff;
import com.feadev.eaids.academy.model.AcademyTrainingSchedule;
import com.feadev.eaids.academy.model.enums.StaffType;
import com.feadev.eaids.academy.repository.AcademyScheduleStaffRepository;
import com.feadev.eaids.academy.repository.AcademyTrainingScheduleRepository;
import com.feadev.eaids.academy.service.AcademyScheduleStaffService;
import com.feadev.eaids.registrations.exception.DuplicateResourceException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.CoachProfile;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.repository.CoachProfileRepository;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AuditService;
import com.feadev.eaids.scouting.model.ScoutProfile;
import com.feadev.eaids.scouting.repository.ScoutProfileRepository;
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
public class AcademyScheduleStaffServiceImpl implements AcademyScheduleStaffService {

    private final AcademyScheduleStaffRepository staffRepository;
    private final AcademyTrainingScheduleRepository scheduleRepository;
    private final CoachProfileRepository coachProfileRepository;
    private final ScoutProfileRepository scoutProfileRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ScheduleStaffResponse assignStaff(UUID scheduleId, AssignStaffRequest request) {
        AcademyTrainingSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Training schedule not found: " + scheduleId));

        String staffEmail;
        String staffName;
        AcademyScheduleStaff.AcademyScheduleStaffBuilder builder = AcademyScheduleStaff.builder()
                .schedule(schedule)
                .staffType(request.getStaffType());

        if (request.getStaffType() == StaffType.COACH) {
            CoachProfile coach = coachProfileRepository.findById(request.getProfileId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Coach profile not found: " + request.getProfileId()));
            if (staffRepository.existsByScheduleIdAndCoachProfileId(scheduleId, coach.getId())) {
                throw new DuplicateResourceException(
                        "This coach is already assigned to the schedule");
            }
            builder.coachProfileId(coach.getId());
            staffName = coach.getFullName();
            staffEmail = coach.getUser() != null ? coach.getUser().getEmail() : null;
        } else {
            ScoutProfile scout = scoutProfileRepository.findById(request.getProfileId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Scout profile not found: " + request.getProfileId()));
            if (staffRepository.existsByScheduleIdAndScoutProfileId(scheduleId, scout.getId())) {
                throw new DuplicateResourceException(
                        "This scout is already assigned to the schedule");
            }
            builder.scoutProfileId(scout.getId());
            staffName = scout.getFullName();
            staffEmail = userRepository.findById(scout.getUserId())
                    .map(User::getEmail)
                    .orElse(null);
        }

        AcademyScheduleStaff saved = staffRepository.save(builder.build());

        if (staffEmail != null) {
            eventPublisher.publishEvent(new ScheduleStaffAssignedEvent(
                    scheduleId, staffEmail, staffName, schedule.getTitle()));
        } else {
            log.warn("Assigned staff {} has no resolvable email; assignment email skipped", staffName);
        }
        audit(AuditAction.ACADEMY_STAFF_ASSIGNED);
        log.info("Academy staff assigned: scheduleId={}, staffType={}, profileId={}",
                scheduleId, request.getStaffType(), request.getProfileId());

        return toResponse(saved, staffName, staffEmail);
    }

    @Override
    public void removeStaff(UUID scheduleId, UUID staffAssignmentId) {
        AcademyScheduleStaff staff = staffRepository.findById(staffAssignmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Staff assignment not found: " + staffAssignmentId));
        if (staff.getSchedule() == null || !staff.getSchedule().getId().equals(scheduleId)) {
            throw new ResourceNotFoundException(
                    "Staff assignment " + staffAssignmentId + " does not belong to schedule " + scheduleId);
        }
        staffRepository.delete(staff);
        audit(AuditAction.ACADEMY_STAFF_REMOVED);
        log.info("Academy staff removed: scheduleId={}, staffAssignmentId={}", scheduleId, staffAssignmentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleStaffResponse> listStaff(UUID scheduleId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new ResourceNotFoundException("Training schedule not found: " + scheduleId);
        }
        return staffRepository.findByScheduleId(scheduleId).stream()
                .map(this::toResponse)
                .toList();
    }

    private ScheduleStaffResponse toResponse(AcademyScheduleStaff staff) {
        String name = null;
        String email = null;
        if (staff.getStaffType() == StaffType.COACH && staff.getCoachProfileId() != null) {
            CoachProfile coach = coachProfileRepository.findById(staff.getCoachProfileId()).orElse(null);
            if (coach != null) {
                name = coach.getFullName();
                email = coach.getUser() != null ? coach.getUser().getEmail() : null;
            }
        } else if (staff.getStaffType() == StaffType.SCOUT && staff.getScoutProfileId() != null) {
            ScoutProfile scout = scoutProfileRepository.findById(staff.getScoutProfileId()).orElse(null);
            if (scout != null) {
                name = scout.getFullName();
                email = userRepository.findById(scout.getUserId()).map(User::getEmail).orElse(null);
            }
        }
        return toResponse(staff, name, email);
    }

    private ScheduleStaffResponse toResponse(AcademyScheduleStaff staff, String name, String email) {
        UUID profileId = staff.getStaffType() == StaffType.COACH
                ? staff.getCoachProfileId() : staff.getScoutProfileId();
        return ScheduleStaffResponse.builder()
                .id(staff.getId())
                .scheduleId(staff.getSchedule() != null ? staff.getSchedule().getId() : null)
                .staffType(staff.getStaffType() != null ? staff.getStaffType().name() : null)
                .profileId(profileId)
                .staffName(name)
                .staffEmail(email)
                .assignedDate(staff.getAssignedDate())
                .build();
    }

    private void audit(AuditAction action) {
        String email = SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName() : null;
        auditService.log(action, null, email, null, null);
    }
}
