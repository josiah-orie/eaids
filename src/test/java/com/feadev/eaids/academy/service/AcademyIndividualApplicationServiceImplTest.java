package com.feadev.eaids.academy.service;

import com.feadev.eaids.academy.dto.request.AddressDto;
import com.feadev.eaids.academy.dto.request.CreateIndividualApplicationRequest;
import com.feadev.eaids.academy.dto.response.IndividualApplicationResponse;
import com.feadev.eaids.academy.model.AcademyIndividualApplication;
import com.feadev.eaids.academy.model.enums.ApplicationStatus;
import com.feadev.eaids.academy.model.enums.Location;
import com.feadev.eaids.academy.repository.AcademyIndividualApplicationRepository;
import com.feadev.eaids.academy.service.impl.AcademyIndividualApplicationServiceImpl;
import com.feadev.eaids.registrations.exception.DuplicateResourceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AcademyIndividualApplicationServiceImplTest {

    @Mock
    private AcademyIndividualApplicationRepository repository;

    @InjectMocks
    private AcademyIndividualApplicationServiceImpl service;

    private CreateIndividualApplicationRequest validRequest() {
        return CreateIndividualApplicationRequest.builder()
                .fullName("Ada Obi")
                .age(15)
                .email("ada@example.com")
                .phone("+2348030000000")
                .location(Location.LAGOS)
                .address(AddressDto.builder()
                        .country("Nigeria")
                        .state("Lagos")
                        .lgaOrProvince("Ikeja")
                        .streetAddress("1 Allen Ave")
                        .build())
                .build();
    }

    @Test
    @DisplayName("submit persists a PENDING application and returns it")
    void submitPersistsPending() {
        when(repository.existsByEmail("ada@example.com")).thenReturn(false);
        when(repository.save(any(AcademyIndividualApplication.class)))
                .thenAnswer(inv -> {
                    AcademyIndividualApplication a = inv.getArgument(0);
                    a.setId(UUID.randomUUID());
                    return a;
                });

        IndividualApplicationResponse response = service.submit(validRequest());

        assertThat(response.getId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.PENDING.name());
        assertThat(response.getLocation()).isEqualTo(Location.LAGOS.name());
        assertThat(response.getAddress().getState()).isEqualTo("Lagos");

        verify(repository).save(argThat(a ->
                a.getStatus() == ApplicationStatus.PENDING
                        && a.getEmail().equals("ada@example.com")
                        && a.getAddress() != null));
    }

    @Test
    @DisplayName("submit rejects a duplicate email")
    void submitRejectsDuplicateEmail() {
        when(repository.existsByEmail("ada@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.submit(validRequest()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");

        verify(repository, never()).save(any());
    }
}
