package com.feadev.eaids.event.service.impl;

import com.feadev.eaids.event.dto.request.CreateEventRequest;
import com.feadev.eaids.event.dto.request.UpdateEventRequest;
import com.feadev.eaids.event.dto.response.EventResponse;
import com.feadev.eaids.event.model.Event;
import com.feadev.eaids.event.model.enums.EventStatus;
import com.feadev.eaids.event.repository.EventRepository;
import com.feadev.eaids.registrations.service.StorageService;
import net.jqwik.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Property 2: Preservation — Non-Image Operations Behavior Unchanged.
 *
 * <p><b>Validates: Requirements 3.1, 3.2, 3.3, 3.4</b></p>
 *
 * <p>These tests observe and encode the current behavior of EventServiceImpl for
 * non-image operations and image-related side effects. They must pass on both
 * unfixed and fixed code, confirming no regressions.</p>
 *
 * <p>Observation-first methodology: tests first observe behavior on unfixed code,
 * then encode it as properties that must hold after the fix is applied.</p>
 */
class EventServicePreservationTest {

    private static final String BASE_URL = "http://localhost:8080";

    private EventRepository eventRepository;
    private StorageService storageService;
    private EventServiceImpl eventService;

    private void setup() {
        eventRepository = mock(EventRepository.class);
        storageService = mock(StorageService.class);
        eventService = new EventServiceImpl(eventRepository, storageService);
        ReflectionTestUtils.setField(eventService, "baseUrl", BASE_URL);
    }

    // ==================== Test 1: Create event without image — imagePath is null ====================

    /**
     * For all valid event creation requests without an image file,
     * the resulting imagePath must be null.
     *
     * <p><b>Validates: Requirements 3.1</b></p>
     */
    @Property(tries = 30)
    void createEvent_withoutImage_imagePathIsNull(
            @ForAll("validCreateRequests") CreateEventRequest request
    ) {
        setup();

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setId(UUID.randomUUID());
            event.setCreatedDate(LocalDateTime.now());
            event.setLastModifiedDate(LocalDateTime.now());
            return event;
        });

        EventResponse response = eventService.createEvent(request, null);

        assertThat(response.getImagePath()).isNull();
        assertThat(response.getEventImage()).isNull();
        verifyNoInteractions(storageService);
    }

    // ==================== Test 2: Update event metadata only — imagePath unchanged ====================

    /**
     * For all metadata-only updates on DRAFT events (no new image provided),
     * the existing imagePath must remain unchanged.
     *
     * <p><b>Validates: Requirements 3.2</b></p>
     */
    @Property(tries = 30)
    void updateEvent_metadataOnly_imagePathUnchanged(
            @ForAll("validUpdateRequests") UpdateEventRequest request,
            @ForAll("existingImagePaths") String existingImagePath
    ) {
        setup();

        UUID eventId = UUID.randomUUID();
        Event existingEvent = buildDraftEvent(eventId);
        existingEvent.setImagePath(existingImagePath);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setLastModifiedDate(LocalDateTime.now());
            return event;
        });

        EventResponse response = eventService.updateEvent(eventId, request, null);

        assertThat(response.getImagePath()).isEqualTo(existingImagePath);
        verifyNoInteractions(storageService);
    }

    // ==================== Test 3: Update with new image when old exists — delete called with relative path ====================

    /**
     * When updating an event that has an existing imagePath and providing a new image,
     * storageService.delete() must be called with the relative path extracted from
     * the old imagePath value (stripping the base URL prefix if present).
     *
     * <p><b>Validates: Requirements 3.3</b></p>
     */
    @Property(tries = 20)
    void updateEvent_withNewImage_deletesOldImagePath(
            @ForAll("existingImagePaths") String oldImagePath,
            @ForAll("imageFileNames") String newFileName
    ) {
        setup();

        UUID eventId = UUID.randomUUID();
        Event existingEvent = buildDraftEvent(eventId);
        existingEvent.setImagePath(oldImagePath);
        existingEvent.setEventImage("old-image.jpeg");

        MockMultipartFile newImage = new MockMultipartFile(
                "image", newFileName, "image/jpeg", new byte[]{1, 2, 3, 4});

        String newRelativePath = "events/" + eventId + "/image/" + UUID.randomUUID() + ".jpeg";
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(storageService.store(any(MultipartFile.class), any(String.class))).thenReturn(newRelativePath);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setLastModifiedDate(LocalDateTime.now());
            return event;
        });

        eventService.updateEvent(eventId, UpdateEventRequest.builder().build(), newImage);

        // Verify delete is called with the relative path (base URL stripped if present)
        String expectedDeletePath = extractRelativePathForTest(oldImagePath);
        verify(storageService).delete(expectedDeletePath);
    }

    // ==================== Test 4: StorageService.store() receives correct folder argument ====================

    /**
     * When an image is uploaded during update, storageService.store() must be called
     * with the folder "events/{eventId}/image".
     *
     * <p><b>Validates: Requirements 3.4</b></p>
     */
    @Property(tries = 20)
    void updateEvent_withImage_storeReceivesCorrectFolder(
            @ForAll("imageFileNames") String fileName
    ) {
        setup();

        UUID eventId = UUID.randomUUID();
        Event existingEvent = buildDraftEvent(eventId);

        MockMultipartFile image = new MockMultipartFile(
                "image", fileName, "image/jpeg", new byte[]{1, 2, 3, 4});

        String relativePath = "events/" + eventId + "/image/" + UUID.randomUUID() + ".jpeg";
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(storageService.store(any(MultipartFile.class), any(String.class))).thenReturn(relativePath);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setLastModifiedDate(LocalDateTime.now());
            return event;
        });

        eventService.updateEvent(eventId, UpdateEventRequest.builder().build(), image);

        ArgumentCaptor<String> folderCaptor = ArgumentCaptor.forClass(String.class);
        verify(storageService).store(any(MultipartFile.class), folderCaptor.capture());
        assertThat(folderCaptor.getValue()).isEqualTo("events/" + eventId + "/image");
    }

    // ==================== Test 5: getEvent, getAllEvents, deleteEvent, transitionStatus unchanged ====================

    /**
     * getEvent() returns correct response with all fields mapped from the entity.
     *
     * <p><b>Validates: Requirements 3.1, 3.2</b></p>
     */
    @Property(tries = 20)
    void getEvent_returnsCorrectResponse(
            @ForAll("existingImagePaths") String imagePath
    ) {
        setup();

        UUID eventId = UUID.randomUUID();
        Event event = buildDraftEvent(eventId);
        event.setImagePath(imagePath);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        EventResponse response = eventService.getEvent(eventId);

        assertThat(response.getId()).isEqualTo(eventId);
        assertThat(response.getName()).isEqualTo(event.getName());
        assertThat(response.getImagePath()).isEqualTo(imagePath);
        assertThat(response.getStatus()).isEqualTo(EventStatus.DRAFT.name());
    }

    /**
     * getAllEvents() returns all events with their imagePaths intact.
     *
     * <p><b>Validates: Requirements 3.1</b></p>
     */
    @Property(tries = 10)
    void getAllEvents_returnsAllEventsWithImagePaths(
            @ForAll("smallEventCount") int count
    ) {
        setup();

        List<Event> events = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Event event = buildDraftEvent(UUID.randomUUID());
            event.setImagePath(i % 2 == 0 ? BASE_URL + "/storage/events/" + event.getId() + "/image/test.jpeg" : null);
            events.add(event);
        }

        when(eventRepository.findAll()).thenReturn(events);

        List<EventResponse> responses = eventService.getAllEvents();

        assertThat(responses).hasSize(count);
        for (int i = 0; i < count; i++) {
            assertThat(responses.get(i).getImagePath()).isEqualTo(events.get(i).getImagePath());
        }
    }

    /**
     * deleteEvent() on a DRAFT event succeeds without affecting storage.
     *
     * <p><b>Validates: Requirements 3.1</b></p>
     */
    @Example
    void deleteEvent_draftEvent_succeeds() {
        setup();

        UUID eventId = UUID.randomUUID();
        Event event = buildDraftEvent(eventId);
        event.setImagePath(BASE_URL + "/storage/events/" + eventId + "/image/test.jpeg");

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        eventService.deleteEvent(eventId);

        verify(eventRepository).delete(event);
        verifyNoInteractions(storageService);
    }

    /**
     * transitionStatus() from DRAFT to REGISTRATION_OPEN succeeds and preserves imagePath.
     *
     * <p><b>Validates: Requirements 3.2</b></p>
     */
    @Example
    void transitionStatus_validTransition_preservesImagePath() {
        setup();

        UUID eventId = UUID.randomUUID();
        Event event = buildDraftEvent(eventId);
        String existingImagePath = BASE_URL + "/storage/events/" + eventId + "/image/test.jpeg";
        event.setImagePath(existingImagePath);

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponse response = eventService.transitionStatus(eventId, EventStatus.REGISTRATION_OPEN);

        assertThat(response.getStatus()).isEqualTo(EventStatus.REGISTRATION_OPEN.name());
        assertThat(response.getImagePath()).isEqualTo(existingImagePath);
        verifyNoInteractions(storageService);
    }

    // ==================== Providers ====================

    @Provide
    Arbitrary<CreateEventRequest> validCreateRequests() {
        Arbitrary<String> names = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .ofMinLength(3).ofMaxLength(30);

        Arbitrary<String> eventTypes = Arbitraries.of("Showcase", "Combine", "Camp", "Tryout");

        Arbitrary<String> locations = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withChars(' ')
                .ofMinLength(3).ofMaxLength(30);

        Arbitrary<String> countries = Arbitraries.of("Nigeria", "Ghana", "Kenya", "South Africa");

        Arbitrary<Integer> capacities = Arbitraries.integers().between(1, 5000);

        Arbitrary<Integer> dayOffsets = Arbitraries.integers().between(1, 180);

        return Combinators.combine(names, eventTypes, locations, countries, capacities, dayOffsets)
                .as((name, type, location, country, capacity, offset) ->
                        CreateEventRequest.builder()
                                .name(name)
                                .description("Test description")
                                .eventType(type)
                                .location(location)
                                .country(country)
                                .state("Lagos")
                                .startDate(LocalDate.now().plusDays(offset))
                                .endDate(LocalDate.now().plusDays(offset + 1))
                                .startTime(LocalTime.of(9, 0))
                                .endTime(LocalTime.of(17, 0))
                                .capacity(capacity)
                                .build()
                );
    }

    @Provide
    Arbitrary<UpdateEventRequest> validUpdateRequests() {
        Arbitrary<String> names = Arbitraries.of("Updated Name A", "New Event Name", null);
        Arbitrary<String> descriptions = Arbitraries.of("New desc", "Longer description text", null);
        Arbitrary<String> eventTypes = Arbitraries.of("Showcase", "Combine", null);
        Arbitrary<String> locations = Arbitraries.of("New Stadium", "Training Center", null);
        Arbitrary<String> countries = Arbitraries.of("Nigeria", "Ghana", null);
        Arbitrary<Integer> capacities = Arbitraries.of(50, 200, 1000, null);

        return Combinators.combine(names, descriptions, eventTypes, locations, countries, capacities)
                .as((name, desc, type, location, country, capacity) ->
                        UpdateEventRequest.builder()
                                .name(name)
                                .description(desc)
                                .eventType(type)
                                .location(location)
                                .country(country)
                                .capacity(capacity)
                                .build()
                );
    }

    @Provide
    Arbitrary<String> existingImagePaths() {
        return Arbitraries.of(
                BASE_URL + "/storage/events/54216d52-abc/image/495f53a4-def.jpeg",
                BASE_URL + "/storage/events/12345678-1234/image/abcdefgh.png",
                BASE_URL + "/storage/events/uuid-here/image/photo.jpeg",
                BASE_URL + "/storage/events/a1b2c3d4-e5f6/image/banner.png",
                BASE_URL + "/storage/events/98765432-wxyz/image/event-photo.jpeg"
        );
    }

    @Provide
    Arbitrary<String> imageFileNames() {
        return Arbitraries.of(
                "photo.jpeg", "image.png", "event-banner.jpeg", "logo.jpg"
        );
    }

    @Provide
    Arbitrary<Integer> smallEventCount() {
        return Arbitraries.integers().between(1, 5);
    }

    // ==================== Helpers ====================

    /**
     * Mirrors the extractRelativePath logic in EventServiceImpl:
     * strips the base URL prefix if present, otherwise returns as-is.
     */
    private String extractRelativePathForTest(String absoluteUrl) {
        String base = BASE_URL.endsWith("/") ? BASE_URL : BASE_URL + "/";
        String storagePrefix = base + "storage/";
        if (absoluteUrl.startsWith(storagePrefix)) {
            return absoluteUrl.substring(storagePrefix.length());
        }
        if (absoluteUrl.startsWith(base)) {
            return absoluteUrl.substring(base.length());
        }
        return absoluteUrl; // fallback: return as-is if not prefixed
    }

    private Event buildDraftEvent(UUID eventId) {
        return Event.builder()
                .id(eventId)
                .name("Test Event")
                .description("Test Description")
                .eventType("Showcase")
                .status(EventStatus.DRAFT)
                .location("Lagos Stadium")
                .country("Nigeria")
                .state("Lagos")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(11))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .capacity(100)
                .currentRegistrationCount(0)
                .createdDate(LocalDateTime.now().minusDays(1))
                .lastModifiedDate(LocalDateTime.now().minusDays(1))
                .build();
    }
}
