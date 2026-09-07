package com.feadev.eaids.academy.service;

import com.feadev.eaids.academy.dto.request.AddressDto;
import com.feadev.eaids.academy.dto.request.CreateInstitutionApplicationRequest;
import com.feadev.eaids.academy.dto.response.InstitutionApplicationResponse;
import com.feadev.eaids.academy.model.AcademyInstitutionApplication;
import com.feadev.eaids.academy.model.enums.ApplicationStatus;
import com.feadev.eaids.academy.model.enums.InstitutionType;
import com.feadev.eaids.academy.repository.AcademyInstitutionApplicationRepository;
import com.feadev.eaids.academy.service.impl.AcademyInstitutionApplicationServiceImpl;
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
class AcademyInstitutionApplicationServiceImplTest {

    @Mock
    private AcademyInstitutionApplicationRepository repository;

    @InjectMocks
    private AcademyInstitutionApplicationServiceImpl service;

    private CreateInstitutionApplicationRequest validRequest() {
        return CreateInstitutionApplicationRequest.builder()
                .institutionType(InstitutionType.UNIVERSITY)
                .name("University of Lagos")
                .contactEmail("sports@unilag.edu.ng")
                .contactPhone("+2348050000000")
                .address(AddressDto.builder()
                        .country("Nigeria")
                        .state("Lagos")
                        .lgaOrProvince("Akoka")
                        .streetAddress("University Road")
                        .build())
                .build();
    }

    @Test
    @DisplayName("submit persists a PENDING institution application and returns it")
    void submitPersistsPending() {
        when(repository.existsByContactEmail("sports@unilag.edu.ng")).thenReturn(false);
        when(repository.save(any(AcademyInstitutionApplication.class)))
                .thenAnswer(inv -> {
                    AcademyInstitutionApplication a = inv.getArgument(0);
                    a.setId(UUID.randomUUID());
                    return a;
                });

        InstitutionApplicationResponse response = service.submit(validRequest());

        assertThat(response.getId()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(ApplicationStatus.PENDING.name());
        assertThat(response.getInstitutionType()).isEqualTo(InstitutionType.UNIVERSITY.name());
        assertThat(response.getAddress().getLgaOrProvince()).isEqualTo("Akoka");

        verify(repository).save(argThat(a ->
                a.getStatus() == ApplicationStatus.PENDING
                        && a.getContactEmail().equals("sports@unilag.edu.ng")
                        && a.getAddress() != null));
    }

    @Test
    @DisplayName("submit rejects a duplicate contact email")
    void submitRejectsDuplicateContactEmail() {
        when(repository.existsByContactEmail("sports@unilag.edu.ng")).thenReturn(true);

        assertThatThrownBy(() -> service.submit(validRequest()))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("contact email");

        verify(repository, never()).save(any());
    }
}
