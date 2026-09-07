package com.feadev.eaids.academy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feadev.eaids.academy.dto.request.AddressDto;
import com.feadev.eaids.academy.dto.request.CreateInstitutionApplicationRequest;
import com.feadev.eaids.academy.dto.response.InstitutionApplicationResponse;
import com.feadev.eaids.academy.model.enums.InstitutionType;
import com.feadev.eaids.academy.service.AcademyInstitutionApplicationService;
import com.feadev.eaids.registrations.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AcademyInstitutionApplicationControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AcademyInstitutionApplicationService service;

    @InjectMocks
    private AcademyInstitutionApplicationController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

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
    @DisplayName("POST valid institution application returns 201")
    void submitValidReturns201() throws Exception {
        when(service.submit(any())).thenReturn(InstitutionApplicationResponse.builder()
                .id(UUID.randomUUID())
                .name("University of Lagos")
                .status("PENDING")
                .build());

        mockMvc.perform(post("/api/v1/academy/applications/institutions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(service).submit(any());
    }

    @Test
    @DisplayName("POST with missing required fields returns 400")
    void submitInvalidReturns400() throws Exception {
        CreateInstitutionApplicationRequest bad = validRequest();
        bad.setName(null);
        bad.setContactEmail(null);

        mockMvc.perform(post("/api/v1/academy/applications/institutions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        verify(service, never()).submit(any());
    }
}
