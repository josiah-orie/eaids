package com.feadev.eaids.academy.service;

import com.feadev.eaids.academy.dto.request.AssignStaffRequest;
import com.feadev.eaids.academy.dto.response.ScheduleStaffResponse;
import com.feadev.eaids.academy.event.ScheduleStaffAssignedEvent;
import com.feadev.eaids.academy.model.AcademyScheduleStaff;
import com.feadev.eaids.academy.model.AcademyTrainingSchedule;
import com.feadev.eaids.academy.model.enums.StaffType;
import com.feadev.eaids.academy.repository.AcademyScheduleStaffRepository;
import com.feadev.eaids.academy.repository.AcademyTrainingScheduleRepository;
import com.feadev.eaids.academy.service.impl.AcademyScheduleStaffServiceImpl;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademyScheduleStaffServiceImplTest {

    @Mock private AcademyScheduleStaffRepository staffRepository;
    @Mock private AcademyTrainingScheduleRepository scheduleRepository;
    @Mock private CoachProfileRepository coachProfileRepository;
    @Mock private ScoutProfileRepository scoutProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private AuditService auditService;
    @Mock private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AcademyScheduleStaffServiceImpl service;

    private AcademyTrainingSchedule schedule() {
        return AcademyTrainingSchedule.builder()
                .id(UUID.randomUUID())
                .title("U16 Camp")
                .build();
    }

    @Test
    @DisplayName("assign coach validates profile, prevents dup, publishes event and audits")
    void assignCoach() {
        AcademyTrainingSchedule schedule = schedule();
        UUID coachProfileId = UUID.randomUUID();
        CoachProfile coach = CoachProfile.builder()
                .id(coachProfileId)
                .fullName("Coach Bello")
                .user(User.builder().id(UUID.randomUUID()).email("bello@eaids.com").build())
                .build();

        when(scheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));
        when(coachProfileRepository.findById(coachProfileId)).thenReturn(Optional.of(coach));
        when(staffRepository.existsByScheduleIdAndCoachProfileId(schedule.getId(), coachProfileId))
                .thenReturn(false);
        when(staffRepository.save(any())).thenAnswer(inv -> {
            AcademyScheduleStaff s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        ScheduleStaffResponse response = service.assignStaff(schedule.getId(),
                AssignStaffRequest.builder().staffType(StaffType.COACH).profileId(coachProfileId).build());

        assertThat(response.getStaffType()).isEqualTo("COACH");
        assertThat(response.getStaffEmail()).isEqualTo("bello@eaids.com");
        verify(eventPublisher).publishEvent(any(ScheduleStaffAssignedEvent.class));
        verify(auditService).log(eq(AuditAction.ACADEMY_STAFF_ASSIGNED), any(), any(), any(), any());
    }

    @Test
    @DisplayName("assign scout resolves email via user repository")
    void assignScout() {
        AcademyTrainingSchedule schedule = schedule();
        UUID scoutProfileId = UUID.randomUUID();
        UUID scoutUserId = UUID.randomUUID();
        ScoutProfile scout = ScoutProfile.builder()
                .id(scoutProfileId)
                .userId(scoutUserId)
                .fullName("Scout Musa")
                .build();

        when(scheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));
        when(scoutProfileRepository.findById(scoutProfileId)).thenReturn(Optional.of(scout));
        when(staffRepository.existsByScheduleIdAndScoutProfileId(schedule.getId(), scoutProfileId))
                .thenReturn(false);
        when(userRepository.findById(scoutUserId))
                .thenReturn(Optional.of(User.builder().id(scoutUserId).email("musa@eaids.com").build()));
        when(staffRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ScheduleStaffResponse response = service.assignStaff(schedule.getId(),
                AssignStaffRequest.builder().staffType(StaffType.SCOUT).profileId(scoutProfileId).build());

        assertThat(response.getStaffType()).isEqualTo("SCOUT");
        assertThat(response.getStaffEmail()).isEqualTo("musa@eaids.com");
        verify(eventPublisher).publishEvent(any(ScheduleStaffAssignedEvent.class));
    }

    @Test
    @DisplayName("assign rejects duplicate coach assignment")
    void assignDuplicateCoach() {
        AcademyTrainingSchedule schedule = schedule();
        UUID coachProfileId = UUID.randomUUID();
        CoachProfile coach = CoachProfile.builder().id(coachProfileId).fullName("Coach Bello").build();

        when(scheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));
        when(coachProfileRepository.findById(coachProfileId)).thenReturn(Optional.of(coach));
        when(staffRepository.existsByScheduleIdAndCoachProfileId(schedule.getId(), coachProfileId))
                .thenReturn(true);

        assertThatThrownBy(() -> service.assignStaff(schedule.getId(),
                AssignStaffRequest.builder().staffType(StaffType.COACH).profileId(coachProfileId).build()))
                .isInstanceOf(DuplicateResourceException.class);

        verify(staffRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("assign rejects unknown coach profile")
    void assignUnknownCoach() {
        AcademyTrainingSchedule schedule = schedule();
        UUID coachProfileId = UUID.randomUUID();
        when(scheduleRepository.findById(schedule.getId())).thenReturn(Optional.of(schedule));
        when(coachProfileRepository.findById(coachProfileId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.assignStaff(schedule.getId(),
                AssignStaffRequest.builder().staffType(StaffType.COACH).profileId(coachProfileId).build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
