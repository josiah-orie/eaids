package com.feadev.eaids.event.service.impl;

import com.feadev.eaids.event.dto.request.CreateEventRequest;
import com.feadev.eaids.event.dto.request.UpdateEventRequest;
import com.feadev.eaids.event.dto.response.EventResponse;
import com.feadev.eaids.event.model.Event;
import com.feadev.eaids.event.model.enums.EventStatus;
import com.feadev.eaids.event.repository.EventRepository;
import com.feadev.eaids.registrations.service.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Bug Condition Exploration Test — Relative Path Stored Instead of Absolute URL on Image Upload.
 *
 * <p><b>Validates: Requirements 1.1, 1.2, 1.3</b></p>
 *
 * <p>This test encodes the EXPECTED correct behavior: when an image is uploaded during
 * event creation or update, the {@code imagePath} field must contain a fully-qualified
 * absolute URL (starting with the configured base URL), NOT a raw relative storage path.</p>
 *
 * <p><b>Bug Condition:</b> When an image is uploaded (create or update), the system stores
 * only the relative path returned by {@code StorageService.store()} instead of constructing
 * an absolute URL by prepending the configured base URL.</p>
 *
 * <p><b>CRITICAL:</b> These tests are expected to FAIL on unfixed code (specifically
 * updateEvent) — failure confirms the bug exists. Do NOT attempt to fix the test or the
 * code when it fails.</p>
 *
 * <p><b>Expected Counterexamples on unfixed code:</b></p>
 * <ul>
 *   <li>updateEvent: {@code imagePath} equals raw relative path like
 *       {@code events/uuid/image/uuid.jpeg} instead of absolute URL</li>
 *   <li>updateEvent: {@code imagePath} does not start with the configured base URL</li>
 * </ul>
 */
@ExtendWith(MockitoExtension.class)
class EventServiceImageUrlBugConditionTest {

    private static final String BASE_URL = "http://localhost:8082";

    @Mock
    private EventRepository eventRepository;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(eventService, "baseUrl", BASE_URL);
    }

    /**
     * Test 1: Call createEvent() with a valid JPEG mock image.
     * Assert imagePath starts with configured base URL + "/"
     *
     * <p>On current code: This MAY PASS because createEvent already prepends baseUrl.</p>
     */
    @Test
    @DisplayName("Bug Condition: createEvent with image should store absolute URL in imagePath")
    void createEvent_withImage_shouldStoreAbsoluteUrlInImagePath() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        String relativePath = "events/" + eventId + "/image/495f53a4-test.jpeg";

        CreateEventRequest request = CreateEventRequest.builder()
                .name("Lagos Showcase 2025")
                .description("Annual football showcase")
                .eventType("Showcase")
                .location("Teslim Balogun Stadium")
                .country("Nigeria")
                .state("Lagos")
                .startDate(LocalDate.now().plusDays(30))
                .endDate(LocalDate.now().plusDays(31))
                .capacity(500)
                .build();

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "event-banner.jpeg",
                "image/jpeg",
                "fake-jpeg-content".getBytes()
        );

        // Mock: first save returns event with ID, second save returns event with imagePath
        Event savedEvent = Event.builder()
                .id(eventId)
                .name(request.getName())
                .description(request.getDescription())
                .eventType(request.getEventType())
                .status(EventStatus.DRAFT)
                .location(request.getLocation())
                .country(request.getCountry())
                .state(request.getState())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .capacity(request.getCapacity())
                .currentRegistrationCount(0)
                .build();

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event e = invocation.getArgument(0);
            if (e.getId() == null) {
                e.setId(eventId);
            }
            return e;
        });

        when(storageService.store(any(), anyString())).thenReturn(relativePath);

        // Act
        EventResponse response = eventService.createEvent(request, image);

        // Assert: imagePath must be a fully-qualified absolute URL
        assertThat(response.getImagePath())
                .as("imagePath should start with the configured base URL")
                .startsWith(BASE_URL + "/");

        assertThat(response.getImagePath())
                .as("imagePath should contain the relative storage path")
                .contains(relativePath);

        assertThat(response.getImagePath())
                .as("imagePath should be: baseUrl + '/' + relativePath")
                .isEqualTo(BASE_URL + "/" + relativePath);
    }

    /**
     * Test 2: Call updateEvent() with a new PNG mock image on a DRAFT event.
     * Assert imagePath starts with base URL + "/"
     *
     * <p>On current (unfixed) code: Expected to FAIL because updateEvent stores only
     * the raw relative path returned by StorageService.store().</p>
     */
    @Test
    @DisplayName("Bug Condition: updateEvent with new image should store absolute URL in imagePath")
    void updateEvent_withNewImage_shouldStoreAbsoluteUrlInImagePath() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        String relativePath = "events/" + eventId + "/image/7a8b9c0d-new.png";

        Event existingEvent = Event.builder()
                .id(eventId)
                .name("Draft Event")
                .description("A draft event")
                .eventType("Combine")
                .status(EventStatus.DRAFT)
                .location("National Stadium")
                .country("Nigeria")
                .state("FCT")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(11))
                .capacity(200)
                .currentRegistrationCount(0)
                .imagePath(null) // no existing image
                .build();

        UpdateEventRequest updateRequest = UpdateEventRequest.builder()
                .name("Updated Event Name")
                .build();

        MockMultipartFile newImage = new MockMultipartFile(
                "image",
                "new-banner.png",
                "image/png",
                "fake-png-content".getBytes()
        );

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(storageService.store(any(), anyString())).thenReturn(relativePath);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EventResponse response = eventService.updateEvent(eventId, updateRequest, newImage);

        // Assert: imagePath must be an absolute URL, NOT a raw relative path
        assertThat(response.getImagePath())
                .as("imagePath should start with the configured base URL (bug: stores relative path instead)")
                .startsWith(BASE_URL + "/");

        assertThat(response.getImagePath())
                .as("imagePath should contain the relative storage path after the base URL")
                .contains(relativePath);

        assertThat(response.getImagePath())
                .as("imagePath should be: baseUrl + '/' + relativePath")
                .isEqualTo(BASE_URL + "/" + relativePath);
    }

    /**
     * Test 3: Assert imagePath is a valid URL format (no double slashes except in "https://")
     *
     * <p>On current (unfixed) code: Expected to FAIL because imagePath from updateEvent
     * is not a URL at all — it's just a relative path.</p>
     */
    @Test
    @DisplayName("Bug Condition: imagePath from updateEvent should be valid URL format (no double slashes)")
    void updateEvent_withImage_imagePathShouldBeValidUrlFormat() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        String relativePath = "events/" + eventId + "/image/abc123.jpeg";

        Event existingEvent = Event.builder()
                .id(eventId)
                .name("Format Test Event")
                .description("Testing URL format")
                .eventType("Showcase")
                .status(EventStatus.DRAFT)
                .location("Lagos Arena")
                .country("Nigeria")
                .state("Lagos")
                .startDate(LocalDate.now().plusDays(20))
                .endDate(LocalDate.now().plusDays(21))
                .capacity(100)
                .currentRegistrationCount(0)
                .imagePath(null)
                .build();

        UpdateEventRequest updateRequest = UpdateEventRequest.builder().build();

        MockMultipartFile image = new MockMultipartFile(
                "image",
                "photo.jpeg",
                "image/jpeg",
                "fake-jpeg".getBytes()
        );

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(storageService.store(any(), anyString())).thenReturn(relativePath);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EventResponse response = eventService.updateEvent(eventId, updateRequest, image);

        // Assert: imagePath must start with http:// or https://
        assertThat(response.getImagePath())
                .as("imagePath should start with http:// or https:// (bug: it's a relative path)")
                .matches("^https?://.*");

        // Assert: No double slashes in path portion (after protocol "://")
        String pathPortion = response.getImagePath().replaceFirst("^https?://", "");
        assertThat(pathPortion)
                .as("URL path portion should not contain double slashes")
                .doesNotContain("//");
    }
}
