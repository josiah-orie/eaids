package com.feadev.eaids.event;

import com.feadev.eaids.event.controller.EventController;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Bug Condition Exploration Test — Event Multipart Form Data Binding with Image Upload.
 *
 * <p><b>Validates: Requirements 1.1, 1.2, 1.3, 2.1, 2.2, 2.3</b></p>
 *
 * <p>This test encodes the EXPECTED (correct) behavior: the event endpoints should accept
 * multipart/form-data with form fields bound via @ModelAttribute and an optional eventImage
 * file part via @RequestPart. On UNFIXED code (with @RequestBody), these tests FAIL with
 * HTTP 415 HttpMediaTypeNotSupportedException — confirming the bug exists.</p>
 *
 * <p><b>Bug Condition:</b> When a client sends multipart/form-data to POST /api/v1/events
 * or PUT /api/v1/events/{id}, the @RequestBody annotation triggers JSON deserialization
 * which fails for multipart content.</p>
 *
 * <p><b>Expected Counterexamples on unfixed code:</b></p>
 * <ul>
 *   <li>Multipart POST to /api/v1/events returns 415 (HttpMediaTypeNotSupportedException)</li>
 *   <li>Multipart PUT to /api/v1/events/{id} returns 415 (HttpMediaTypeNotSupportedException)</li>
 *   <li>Service layer is never reached due to controller binding failure</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class EventImageUploadBugConditionTest {

    private static final String BASE_URL = "http://localhost:8080";

    private MockMvc mockMvc;

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
    }

    /**
     * Test: POST /api/v1/events with multipart/form-data form fields + image file
     * should return 201 with imagePath populated.
     *
     * <p>On UNFIXED code: Expected to FAIL with 415 because @RequestBody cannot handle
     * multipart/form-data content type.</p>
     *
     * <p>After fix (@ModelAttribute): Form fields are bound via setters, image via @RequestPart,
     * returning 201 with valid imagePath.</p>
     */
    @Test
    @DisplayName("Bug Condition: POST multipart with image should return 201 with valid imagePath")
    void createEventWithMultipartImage_shouldReturn201WithImagePath() throws Exception {
        // Arrange: image file part using the correct parameter name "eventImage"
        MockMultipartFile imageFile = new MockMultipartFile(
                "eventImage",
                "event-banner.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-jpeg-content".getBytes()
        );

        // Mock service response — service now returns absolute URL for imagePath
        UUID eventId = UUID.randomUUID();
        EventResponse expectedResponse = EventResponse.builder()
                .id(eventId)
                .name("Lagos Showcase 2025")
                .eventType("Showcase")
                .status(EventStatus.DRAFT.name())
                .location("Teslim Balogun Stadium")
                .country("Nigeria")
                .state("Lagos")
                .startDate(LocalDate.now().plusDays(30))
                .endDate(LocalDate.now().plusDays(31))
                .capacity(500)
                .imagePath(BASE_URL + "/storage/events/" + eventId + "/image/event-banner.jpg")
                .eventImage("event-banner.jpg")
                .build();

        when(eventService.createEvent(any(CreateEventRequest.class), any(MultipartFile.class)))
                .thenReturn(expectedResponse);

        // Act & Assert: multipart POST with form fields + image should return 201
        MvcResult result = mockMvc.perform(multipart("/api/v1/events")
                        .file(imageFile)
                        .param("name", "Lagos Showcase 2025")
                        .param("description", "Annual football showcase event")
                        .param("eventType", "Showcase")
                        .param("location", "Teslim Balogun Stadium")
                        .param("country", "Nigeria")
                        .param("state", "Lagos")
                        .param("startDate", LocalDate.now().plusDays(30).toString())
                        .param("endDate", LocalDate.now().plusDays(31).toString())
                        .param("capacity", "500")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imagePath").exists())
                .andExpect(jsonPath("$.imagePath").value(
                        org.hamcrest.Matchers.startsWith("http")))
                .andReturn();

        // Verify response has a proper absolute URL imagePath
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("\"imagePath\"");
    }

    /**
     * Test: PUT /api/v1/events/{id} with multipart/form-data form fields + image file
     * should return 200 with updated imagePath.
     *
     * <p>On UNFIXED code: Expected to FAIL with 415 because @RequestBody cannot handle
     * multipart/form-data on the update endpoint.</p>
     */
    @Test
    @DisplayName("Bug Condition: PUT multipart with image should return 200 with updated imagePath")
    void updateEventWithMultipartImage_shouldReturn200WithUpdatedImagePath() throws Exception {
        UUID eventId = UUID.randomUUID();

        // Arrange: image file
        MockMultipartFile imageFile = new MockMultipartFile(
                "eventImage",
                "new-banner.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-png-content".getBytes()
        );

        // Mock service response — service now returns absolute URL for imagePath
        EventResponse expectedResponse = EventResponse.builder()
                .id(eventId)
                .name("Updated Lagos Showcase 2025")
                .eventType("Showcase")
                .status(EventStatus.DRAFT.name())
                .location("Teslim Balogun Stadium")
                .country("Nigeria")
                .imagePath(BASE_URL + "/storage/events/" + eventId + "/image/new-banner.png")
                .eventImage("new-banner.png")
                .build();

        when(eventService.updateEvent(any(UUID.class), any(UpdateEventRequest.class), any(MultipartFile.class)))
                .thenReturn(expectedResponse);

        // Act & Assert: multipart PUT with form fields + image should return 200
        mockMvc.perform(multipart("/api/v1/events/{eventId}", eventId)
                        .file(imageFile)
                        .param("name", "Updated Lagos Showcase 2025")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagePath").exists())
                .andExpect(jsonPath("$.imagePath").value(
                        org.hamcrest.Matchers.startsWith("http")));
    }

    /**
     * Test: When an image file is provided during event creation, the service should be
     * invoked with both the request DTO and the MultipartFile, and the response must
     * contain a valid imagePath.
     *
     * <p>On UNFIXED code: Expected to FAIL because @RequestBody causes 415 before the
     * service is ever reached.</p>
     */
    @Test
    @DisplayName("Bug Condition: Service should be invoked and response should have imagePath from StorageService")
    void createEventWithImage_responseShouldContainStoragePath() throws Exception {
        // Arrange: image file
        MockMultipartFile imageFile = new MockMultipartFile(
                "eventImage",
                "combine-photo.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-jpeg-data-for-combine".getBytes()
        );

        UUID eventId = UUID.randomUUID();
        EventResponse mockResponse = EventResponse.builder()
                .id(eventId)
                .name("Abuja Combine 2025")
                .eventType("Combine")
                .status(EventStatus.DRAFT.name())
                .location("National Stadium Abuja")
                .country("Nigeria")
                .imagePath(BASE_URL + "/storage/events/" + eventId + "/image/combine-photo.jpg")
                .eventImage("combine-photo.jpg")
                .build();

        when(eventService.createEvent(any(CreateEventRequest.class), any(MultipartFile.class)))
                .thenReturn(mockResponse);

        // Act: multipart POST with form fields + image
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
                .andExpect(jsonPath("$.imagePath").value(
                        org.hamcrest.Matchers.startsWith("http")))
                .andExpect(jsonPath("$.eventImage").value("combine-photo.jpg"));

        // Verify service was invoked with both arguments
        verify(eventService, times(1)).createEvent(any(CreateEventRequest.class), any(MultipartFile.class));
    }
}
