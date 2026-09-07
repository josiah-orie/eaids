package com.feadev.eaids.academy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.feadev.eaids.academy.dto.request.AddressDto;
import com.feadev.eaids.academy.dto.request.CreateIndividualApplicationRequest;
import com.feadev.eaids.academy.dto.response.IndividualApplicationResponse;
import com.feadev.eaids.academy.model.enums.Location;
import com.feadev.eaids.academy.service.AcademyIndividualApplicationService;
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
class AcademyIndividualApplicationControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AcademyIndividualApplicationService service;

    @InjectMocks
    private AcademyIndividualApplicationController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

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
    @DisplayName("POST valid individual application returns 201")
    void submitValidReturns201() throws Exception {
        when(service.submit(any())).thenReturn(IndividualApplicationResponse.builder()
                .id(UUID.randomUUID())
                .fullName("Ada Obi")
                .status("PENDING")
                .build());

        mockMvc.perform(post("/api/v1/academy/applications/individuals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(service).submit(any());
    }

    @Test
    @DisplayName("POST with missing required fields returns 400")
    void submitInvalidReturns400() throws Exception {
        CreateIndividualApplicationRequest bad = validRequest();
        bad.setEmail(null);
        bad.setFullName(null);

        mockMvc.perform(post("/api/v1/academy/applications/individuals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        verify(service, never()).submit(any());
    }
}
