package com.feadev.eaids.academy.service;

import com.feadev.eaids.academy.dto.request.ReviewApplicationRequest;
import com.feadev.eaids.academy.event.IndividualApplicationApprovedEvent;
import com.feadev.eaids.academy.event.IndividualApplicationRejectedEvent;
import com.feadev.eaids.academy.model.AcademyIndividualApplication;
import com.feadev.eaids.academy.model.enums.ApplicationStatus;
import com.feadev.eaids.academy.model.enums.Location;
import com.feadev.eaids.academy.repository.AcademyIndividualApplicationRepository;
import com.feadev.eaids.academy.repository.AcademyInstitutionApplicationRepository;
import com.feadev.eaids.academy.service.impl.AcademyAdminApplicationServiceImpl;
import com.feadev.eaids.registrations.exception.InvalidStateTransitionException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.model.enums.AuditAction;
import com.feadev.eaids.registrations.repository.UserRepository;
import com.feadev.eaids.registrations.service.AuditService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademyAdminApplicationServiceImplTest {

    @Mock
    private AcademyIndividualApplicationRepository individualRepository;
    @Mock
    private AcademyInstitutionApplicationRepository institutionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditService auditService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AcademyAdminApplicationServiceImpl service;

    private final UUID adminId = UUID.randomUUID();

    @BeforeEach
    void setUpSecurityContext() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin@eaids.com", "n/a"));
        lenient().when(userRepository.findByEmail("admin@eaids.com"))
                .thenReturn(Optional.of(User.builder().id(adminId).email("admin@eaids.com").build()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private AcademyIndividualApplication pendingIndividual() {
        return AcademyIndividualApplication.builder()
                .id(UUID.randomUUID())
                .fullName("Ada Obi")
                .age(15)
                .email("ada@example.com")
                .phone("+2348030000000")
                .location(Location.LAGOS)
                .status(ApplicationStatus.PENDING)
                .build();
    }

    @Test
    @DisplayName("approve sets APPROVED, records reviewer, publishes approved event and audits")
    void approveIndividual() {
        AcademyIndividualApplication app = pendingIndividual();
        when(individualRepository.findById(app.getId())).thenReturn(Optional.of(app));
        when(individualRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReviewApplicationRequest request = ReviewApplicationRequest.builder()
                .decision(ApplicationStatus.APPROVED)
                .build();

        var response = service.reviewIndividual(app.getId(), request);

        assertThat(response.getStatus()).isEqualTo("APPROVED");
        assertThat(app.getReviewedBy()).isEqualTo(adminId);
        assertThat(app.getReviewDate()).isNotNull();
        verify(eventPublisher).publishEvent(any(IndividualApplicationApprovedEvent.class));
        verify(auditService).log(eq(AuditAction.ACADEMY_APPLICATION_APPROVED), eq(adminId), any(), any(), any());
    }

    @Test
    @DisplayName("reject with reason sets REJECTED and publishes rejected event")
    void rejectIndividualWithReason() {
        AcademyIndividualApplication app = pendingIndividual();
        when(individualRepository.findById(app.getId())).thenReturn(Optional.of(app));
        when(individualRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ReviewApplicationRequest request = ReviewApplicationRequest.builder()
                .decision(ApplicationStatus.REJECTED)
                .reviewNotes("Outside catchment area")
                .build();

        var response = service.reviewIndividual(app.getId(), request);

        assertThat(response.getStatus()).isEqualTo("REJECTED");
        assertThat(response.getReviewNotes()).isEqualTo("Outside catchment area");
        verify(eventPublisher).publishEvent(any(IndividualApplicationRejectedEvent.class));
        verify(auditService).log(eq(AuditAction.ACADEMY_APPLICATION_REJECTED), eq(adminId), any(), any(), any());
    }

    @Test
    @DisplayName("reject without reason is rejected")
    void rejectRequiresReason() {
        AcademyIndividualApplication app = pendingIndividual();
        when(individualRepository.findById(app.getId())).thenReturn(Optional.of(app));

        ReviewApplicationRequest request = ReviewApplicationRequest.builder()
                .decision(ApplicationStatus.REJECTED)
                .build();

        assertThatThrownBy(() -> service.reviewIndividual(app.getId(), request))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("reason");

        verify(individualRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("reviewing a non-PENDING application is rejected")
    void reviewGuardsPendingState() {
        AcademyIndividualApplication app = pendingIndividual();
        app.setStatus(ApplicationStatus.APPROVED);
        when(individualRepository.findById(app.getId())).thenReturn(Optional.of(app));

        ReviewApplicationRequest request = ReviewApplicationRequest.builder()
                .decision(ApplicationStatus.APPROVED)
                .build();

        assertThatThrownBy(() -> service.reviewIndividual(app.getId(), request))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("PENDING");

        verify(eventPublisher, never()).publishEvent(any());
    }
}
