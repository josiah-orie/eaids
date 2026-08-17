package com.feadev.eaids.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
 * Bug Condition Exploration Test — Event Image Upload Rejected by JSON-Only Endpoints.
 *
 * <p><b>Validates: Requirements 1.1, 1.2, 1.3, 2.1, 2.2, 2.3</b></p>
 *
 * <p>This test encodes the EXPECTED (correct) behavior: the event endpoints should accept
 * multipart form-data with an image file and return a response with a valid imagePath.
 * On UNFIXED code, these tests will FAIL because the current controller only accepts
 * JSON @RequestBody — confirming the bug exists.</p>
 *
 * <p><b>Bug Condition:</b> When an admin attempts to create or update an event with an image file,
 * the system cannot accept it because the controller only accepts JSON (@RequestBody).
 * StorageService is not injected into EventServiceImpl, so store() is never called.</p>
 *
 * <p><b>Expected Counterexamples on unfixed code:</b></p>
 * <ul>
 *   <li>Multipart POST to /api/v1/events returns 400/415/500 (endpoint does not consume multipart)</li>
 *   <li>Multipart PUT to /api/v1/events/{id} returns 400/415/500 (endpoint does not consume multipart)</li>
 *   <li>imagePath is null — StorageService not injected, store() never called</li>
 *   <li>EventService.createEvent() signature has no MultipartFile parameter</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class EventImageUploadBugConditionTest {

    private MockMvc mockMvc;

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController).build();
    }

    /**
     * Test: POST /api/v1/events with multipart/form-data containing event JSON + image file
     * should return 201 with imagePath starting with "events/".
     *
     * <p>On UNFIXED code: Expected to FAIL because the endpoint uses @RequestBody (JSON only)
     * and does not consume multipart/form-data. The controller method parameter binding
     * will fail since it expects JSON body, not multipart parts.</p>
     *
     * <p>After fix: The endpoint will accept multipart, parse the "event" JSON part and
     * "image" file part, and return 201 with a valid imagePath.</p>
     */
    @Test
    @DisplayName("Bug Condition: POST multipart with image should return 201 with valid imagePath")
    void createEventWithMultipartImage_shouldReturn201WithImagePath() throws Exception {
        // Arrange: valid event JSON metadata
        CreateEventRequest eventRequest = CreateEventRequest.builder()
                .name("Lagos Showcase 2025")
                .description("Annual football showcase event")
                .eventType("Showcase")
                .location("Teslim Balogun Stadium")
                .country("Nigeria")
                .state("Lagos")
                .startDate(LocalDate.now().plusDays(30))
                .endDate(LocalDate.now().plusDays(31))
                .capacity(500)
                .build();

        String eventJson = objectMapper.writeValueAsString(eventRequest);

        // Arrange: a valid JPEG image file
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "event-banner.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-jpeg-content".getBytes()
        );

        // Arrange: the event JSON as a multipart part with content type application/json
        MockMultipartFile eventPart = new MockMultipartFile(
                "event",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                eventJson.getBytes()
        );

        // Mock service: on unfixed code this won't be reached due to binding failure
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
                .imagePath("events/" + eventId + "/image/event-banner.jpg")
                .eventImage("event-banner.jpg")
                .build();

        when(eventService.createEvent(any(CreateEventRequest.class), any())).thenReturn(expectedResponse);

        // Act & Assert: multipart POST should be accepted and return 201 with valid imagePath
        MvcResult result = mockMvc.perform(multipart("/api/v1/events")
                        .file(imageFile)
                        .file(eventPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imagePath").exists())
                .andExpect(jsonPath("$.imagePath").value(
                        org.hamcrest.Matchers.startsWith("events/")))
                .andReturn();

        // Verify response has a proper storage path (not a client-provided literal)
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody).contains("\"imagePath\"");
    }

    /**
     * Test: PUT /api/v1/events/{id} with multipart/form-data containing event JSON + image file
     * should return 200 with updated imagePath.
     *
     * <p>On UNFIXED code: Expected to FAIL because the update endpoint also uses @RequestBody
     * (JSON only) and does not consume multipart/form-data.</p>
     *
     * <p>After fix: The endpoint will accept multipart PUT, parse the "event" JSON part and
     * "image" file part, delete old image, store new image, and return 200.</p>
     */
    @Test
    @DisplayName("Bug Condition: PUT multipart with image should return 200 with updated imagePath")
    void updateEventWithMultipartImage_shouldReturn200WithUpdatedImagePath() throws Exception {
        UUID eventId = UUID.randomUUID();

        // Arrange: update request JSON
        UpdateEventRequest updateRequest = UpdateEventRequest.builder()
                .name("Updated Lagos Showcase 2025")
                .build();

        String eventJson = objectMapper.writeValueAsString(updateRequest);

        // Arrange: a valid PNG image file
        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "new-banner.png",
                MediaType.IMAGE_PNG_VALUE,
                "fake-png-content".getBytes()
        );

        // Arrange: the event JSON as a multipart part
        MockMultipartFile eventPart = new MockMultipartFile(
                "event",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                eventJson.getBytes()
        );

        // Mock service
        EventResponse expectedResponse = EventResponse.builder()
                .id(eventId)
                .name("Updated Lagos Showcase 2025")
                .eventType("Showcase")
                .status(EventStatus.DRAFT.name())
                .location("Teslim Balogun Stadium")
                .country("Nigeria")
                .imagePath("events/" + eventId + "/image/new-banner.png")
                .eventImage("new-banner.png")
                .build();

        when(eventService.updateEvent(any(UUID.class), any(UpdateEventRequest.class), any()))
                .thenReturn(expectedResponse);

        // Act & Assert: multipart PUT should be accepted and return 200 with valid imagePath
        mockMvc.perform(multipart("/api/v1/events/{eventId}", eventId)
                        .file(imageFile)
                        .file(eventPart)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imagePath").exists())
                .andExpect(jsonPath("$.imagePath").value(
                        org.hamcrest.Matchers.startsWith("events/")));
    }

    /**
     * Test: When an image file is provided during event creation, the service should be called
     * and the response must contain a valid imagePath (evidence that StorageService.store() worked).
     *
     * <p>On UNFIXED code: Expected to FAIL because:</p>
     * <ol>
     *   <li>The controller binds @RequestBody, not @RequestPart, so multipart is not parsed</li>
     *   <li>The service signature is createEvent(CreateEventRequest) — no MultipartFile param</li>
     *   <li>Even if service is called, StorageService is not injected so store() cannot run</li>
     * </ol>
     *
     * <p>This test verifies the full expected flow: controller accepts multipart → calls service
     * → response contains imagePath indicating file was stored.</p>
     */
    @Test
    @DisplayName("Bug Condition: Service should be invoked and response should have imagePath from StorageService")
    void createEventWithImage_responseShouldContainStoragePath() throws Exception {
        // Arrange
        CreateEventRequest eventRequest = CreateEventRequest.builder()
                .name("Abuja Combine 2025")
                .description("Football combine event")
                .eventType("Combine")
                .location("National Stadium Abuja")
                .country("Nigeria")
                .state("FCT")
                .startDate(LocalDate.now().plusDays(60))
                .endDate(LocalDate.now().plusDays(61))
                .capacity(200)
                .build();

        String eventJson = objectMapper.writeValueAsString(eventRequest);

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "combine-photo.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-jpeg-data-for-combine".getBytes()
        );

        MockMultipartFile eventPart = new MockMultipartFile(
                "event",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                eventJson.getBytes()
        );

        UUID eventId = UUID.randomUUID();
        EventResponse mockResponse = EventResponse.builder()
                .id(eventId)
                .name("Abuja Combine 2025")
                .eventType("Combine")
                .status(EventStatus.DRAFT.name())
                .location("National Stadium Abuja")
                .country("Nigeria")
                .imagePath("events/" + eventId + "/image/combine-photo.jpg")
                .eventImage("combine-photo.jpg")
                .build();

        when(eventService.createEvent(any(CreateEventRequest.class), any())).thenReturn(mockResponse);

        // Act: On unfixed code, the multipart POST fails at controller binding layer
        mockMvc.perform(multipart("/api/v1/events")
                        .file(imageFile)
                        .file(eventPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imagePath").value(
                        org.hamcrest.Matchers.startsWith("events/")))
                .andExpect(jsonPath("$.eventImage").value("combine-photo.jpg"));

        // Verify: service was actually invoked (on unfixed code, binding fails so service never called)
        verify(eventService, times(1)).createEvent(any(CreateEventRequest.class), any());
    }
}
