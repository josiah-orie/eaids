package com.feadev.eaids.event.controller;

import com.feadev.eaids.event.dto.request.CreateEventRequest;
import com.feadev.eaids.event.dto.request.UpdateEventRequest;
import com.feadev.eaids.event.dto.response.EventResponse;
import com.feadev.eaids.event.model.enums.EventStatus;
import com.feadev.eaids.event.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Bug Condition and Fix Verification Tests for EventController multipart/form-data binding.
 *
 * <p><b>Validates: Requirements 1.1, 1.2, 1.3, 2.1, 2.2, 2.3</b></p>
 *
 * <p>These tests verify that the EventController correctly accepts multipart/form-data requests
 * after the @RequestBody → @ModelAttribute fix. On unfixed code, all tests would fail with HTTP 415
 * because Spring cannot find an HttpMessageConverter for multipart/form-data when @RequestBody is used.</p>
 *
 * <p><b>Bug Condition:</b> POST /api/v1/events or PUT /api/v1/events/{id} with Content-Type: multipart/form-data</p>
 * <p><b>Expected after fix:</b> Form fields are bound via @ModelAttribute, returning 201/200/400 as appropriate</p>
 */
@ExtendWith(MockitoExtension.class)
class EventControllerMultipartTest {

    private MockMvc mockMvc;

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
    }

    // ==================== Bug Condition Tests (Task 1) ====================

    @Test
    @DisplayName("POST /api/v1/events with multipart form fields returns HTTP 201")
    void createEvent_withMultipartFormFields_returns201() throws Exception {
        // Arrange: mock the service to return a valid response
        UUID eventId = UUID.randomUUID();
        EventResponse mockResponse = EventResponse.builder()
                .id(eventId)
                .name("Lagos Showcase 2025")
                .description("Annual football showcase")
                .eventType("Showcase")
                .status(EventStatus.DRAFT.name())
                .location("Teslim Balogun Stadium")
                .country("Nigeria")
                .state("Lagos")
                .startDate(LocalDate.now().plusDays(30))
                .endDate(LocalDate.now().plusDays(31))
                .capacity(500)
                .currentRegistrationCount(0)
                .build();

        when(eventService.createEvent(any(CreateEventRequest.class), any()))
                .thenReturn(mockResponse);

        // Act & Assert: multipart POST with form fields should return 201
        mockMvc.perform(multipart("/api/v1/events")
                        .param("name", "Lagos Showcase 2025")
                        .param("description", "Annual football showcase")
                        .param("eventType", "Showcase")
                        .param("location", "Teslim Balogun Stadium")
                        .param("country", "Nigeria")
                        .param("state", "Lagos")
                        .param("startDate", LocalDate.now().plusDays(30).toString())
                        .param("endDate", LocalDate.now().plusDays(31).toString())
                        .param("capacity", "500")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Lagos Showcase 2025"))
                .andExpect(jsonPath("$.eventType").value("Showcase"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.country").value("Nigeria"));

        verify(eventService).createEvent(any(CreateEventRequest.class), any());
    }

    @Test
    @DisplayName("PUT /api/v1/events/{id} with multipart form fields returns HTTP 200")
    void updateEvent_withMultipartFormFields_returns200() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        EventResponse mockResponse = EventResponse.builder()
                .id(eventId)
                .name("Updated Showcase 2025")
                .eventType("Showcase")
                .status(EventStatus.DRAFT.name())
                .location("National Stadium")
                .country("Nigeria")
                .capacity(1000)
                .build();

        when(eventService.updateEvent(eq(eventId), any(UpdateEventRequest.class), any()))
                .thenReturn(mockResponse);

        // Act & Assert: multipart PUT with form fields should return 200
        mockMvc.perform(multipart("/api/v1/events/{eventId}", eventId)
                        .param("name", "Updated Showcase 2025")
                        .param("location", "National Stadium")
                        .param("capacity", "1000")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Showcase 2025"))
                .andExpect(jsonPath("$.location").value("National Stadium"))
                .andExpect(jsonPath("$.capacity").value(1000));

        verify(eventService).updateEvent(eq(eventId), any(UpdateEventRequest.class), any());
    }

    @Test
    @DisplayName("POST /api/v1/events with missing required fields returns HTTP 400")
    void createEvent_withMissingRequiredFields_returns400() throws Exception {
        // Act & Assert: multipart POST without required fields should return 400 (not 415)
        // On unfixed code this would return 415 because the controller can't even parse multipart
        mockMvc.perform(multipart("/api/v1/events")
                        .param("description", "Some description")
                        // Missing: name, eventType, location, country, startDate, endDate, capacity
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());

        // Service should never be called when validation fails
        verify(eventService, never()).createEvent(any(), any());
    }

    @Test
    @DisplayName("POST /api/v1/events with image file alongside form fields returns HTTP 201")
    void createEvent_withImageAndFormFields_returns201() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        EventResponse mockResponse = EventResponse.builder()
                .id(eventId)
                .name("Abuja Combine 2025")
                .eventType("Combine")
                .status(EventStatus.DRAFT.name())
                .location("National Stadium Abuja")
                .country("Nigeria")
                .state("FCT")
                .startDate(LocalDate.now().plusDays(60))
                .endDate(LocalDate.now().plusDays(61))
                .capacity(200)
                .imagePath("storage/events/" + eventId + "/image/event-banner.jpg")
                .eventImage("event-banner.jpg")
                .build();

        when(eventService.createEvent(any(CreateEventRequest.class), any(MultipartFile.class)))
                .thenReturn(mockResponse);

        // Arrange: image file part
        MockMultipartFile imageFile = new MockMultipartFile(
                "eventImage",
                "event-banner.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-jpeg-content".getBytes()
        );

        // Act & Assert: multipart POST with form fields AND image file should return 201
        mockMvc.perform(multipart("/api/v1/events")
                        .file(imageFile)
                        .param("name", "Abuja Combine 2025")
                        .param("description", "Football combine event")
                        .param("eventType", "Combine")
                        .param("location", "National Stadium Abuja")
                        .param("country", "Nigeria")
                        .param("state", "FCT")
                        .param("startDate", LocalDate.now().plusDays(60).toString())
                        .param("endDate", LocalDate.now().plusDays(61).toString())
                        .param("capacity", "200")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Abuja Combine 2025"))
                .andExpect(jsonPath("$.imagePath").value("storage/events/" + eventId + "/image/event-banner.jpg"))
                .andExpect(jsonPath("$.eventImage").value("event-banner.jpg"));

        verify(eventService).createEvent(any(CreateEventRequest.class), any(MultipartFile.class));
    }

    // ==================== Preservation Tests (Task 2) ====================
    // GET, DELETE, PATCH endpoints are not affected by the @ModelAttribute change.
    // The existing EventPreservationPropertyTest.java covers service-level preservation.
    // The multipart fix only changes annotation on createEvent/updateEvent methods.
}
