package com.feadev.eaids.academy.service;

import com.feadev.eaids.academy.dto.request.CreateTrainingScheduleRequest;
import com.feadev.eaids.academy.dto.response.TrainingScheduleResponse;
import com.feadev.eaids.academy.model.AcademyTrainingSchedule;
import com.feadev.eaids.academy.model.enums.ScheduleStatus;
import com.feadev.eaids.academy.model.enums.ScheduleTargetType;
import com.feadev.eaids.academy.repository.AcademyTrainingScheduleRepository;
import com.feadev.eaids.academy.service.impl.AcademyTrainingScheduleServiceImpl;
import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.service.AuditService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademyTrainingScheduleServiceImplTest {

    @Mock
    private AcademyTrainingScheduleRepository repository;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private AcademyTrainingScheduleServiceImpl service;

    private CreateTrainingScheduleRequest validRequest() {
        return CreateTrainingScheduleRequest.builder()
                .title("U16 Lagos Skills Camp")
                .description("Six-week skills camp")
                .ageCategory(AgeCategory.UNDER_16)
                .targetType(ScheduleTargetType.INDIVIDUAL)
                .startDate(LocalDate.now().plusDays(7))
                .endDate(LocalDate.now().plusDays(49))
                .location("Lagos")
                .capacity(40)
                .build();
    }

    @Test
    @DisplayName("create persists a DRAFT schedule")
    void createPersistsDraft() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TrainingScheduleResponse response = service.create(validRequest());

        assertThat(response.getStatus()).isEqualTo(ScheduleStatus.DRAFT.name());
        assertThat(response.getAgeCategory()).isEqualTo(AgeCategory.UNDER_16.name());
        assertThat(response.getTargetType()).isEqualTo(ScheduleTargetType.INDIVIDUAL.name());
        verify(repository).save(argThat(s -> s.getStatus() == ScheduleStatus.DRAFT));
    }

    @Test
    @DisplayName("create rejects an end date before the start date")
    void createRejectsInvalidDateRange() {
        CreateTrainingScheduleRequest request = validRequest();
        request.setStartDate(LocalDate.now().plusDays(30));
        request.setEndDate(LocalDate.now().plusDays(10));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("End date");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("publish transitions DRAFT to PUBLISHED")
    void publishTransitions() {
        AcademyTrainingSchedule schedule = AcademyTrainingSchedule.builder()
                .id(java.util.UUID.randomUUID())
                .title("Camp")
                .ageCategory(AgeCategory.UNDER_16)
                .targetType(ScheduleTargetType.INDIVIDUAL)
                .startDate(LocalDate.now().plusDays(7))
                .endDate(LocalDate.now().plusDays(49))
                .capacity(40)
                .status(ScheduleStatus.DRAFT)
                .build();
        when(repository.findById(schedule.getId())).thenReturn(java.util.Optional.of(schedule));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TrainingScheduleResponse response = service.publish(schedule.getId());

        assertThat(response.getStatus()).isEqualTo(ScheduleStatus.PUBLISHED.name());
    }

    @Test
    @DisplayName("publish rejects a non-DRAFT schedule")
    void publishGuardsDraft() {
        AcademyTrainingSchedule schedule = AcademyTrainingSchedule.builder()
                .id(java.util.UUID.randomUUID())
                .status(ScheduleStatus.PUBLISHED)
                .build();
        when(repository.findById(schedule.getId())).thenReturn(java.util.Optional.of(schedule));

        assertThatThrownBy(() -> service.publish(schedule.getId()))
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}
