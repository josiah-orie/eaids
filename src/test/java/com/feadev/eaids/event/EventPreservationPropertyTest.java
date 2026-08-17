package com.feadev.eaids.event;

import com.feadev.eaids.event.dto.request.CreateEventRequest;
import com.feadev.eaids.event.dto.request.UpdateEventRequest;
import com.feadev.eaids.event.dto.response.EventResponse;
import com.feadev.eaids.event.model.Event;
import com.feadev.eaids.event.model.enums.EventStatus;
import com.feadev.eaids.event.repository.EventRepository;
import com.feadev.eaids.event.service.impl.EventServiceImpl;
import com.feadev.eaids.registrations.service.StorageService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Property 2: Preservation — Non-Image Event CRUD Operations Unchanged.
 *
 * <p><b>Validates: Requirements 3.1, 3.2, 3.3, 3.4, 3.5</b></p>
 *
 * <p>These property-based tests verify that non-image event operations behave correctly
 * on the UNFIXED code. They establish the baseline behavior that must be preserved
 * after the image upload fix is applied.</p>
 *
 * <p>Observation-first methodology: tests observe and encode the current correct behavior
 * for non-image operations, confirming that createEvent, updateEvent, deleteEvent,
 * and transitionStatus work as expected when no image file is involved.</p>
 */
class EventPreservationPropertyTest {

    private final EventRepository eventRepository = mock(EventRepository.class);
    private final StorageService storageService = mock(StorageService.class);
    private final EventServiceImpl eventService = new EventServiceImpl(eventRepository, storageService);

    /**
     * Valid status transitions as defined in EventServiceImpl.
     */
    private static final Map<EventStatus, Set<EventStatus>> VALID_TRANSITIONS = Map.of(
            EventStatus.DRAFT, Set.of(EventStatus.REGISTRATION_OPEN, EventStatus.CANCELLED),
            EventStatus.REGISTRATION_OPEN, Set.of(EventStatus.REGISTRATION_CLOSED, EventStatus.CANCELLED),
            EventStatus.REGISTRATION_CLOSED, Set.of(EventStatus.IN_PROGRESS, EventStatus.CANCELLED),
            EventStatus.IN_PROGRESS, Set.of(EventStatus.COMPLETED, EventStatus.CANCELLED),
            EventStatus.COMPLETED, Set.of(),
            EventStatus.CANCELLED, Set.of()
    );

    // ==================== Property: Create Without Image Preserves All Fields ====================

    /**
     * For all valid event metadata inputs without image: createEvent(request) returns response
     * with matching fields and imagePath = null.
     *
     * <p><b>Validates: Requirements 3.1</b></p>
     */
    @Property(tries = 50)
    void createEventWithoutImage_preservesAllMetadataFields(
            @ForAll("validCreateRequests") CreateEventRequest request
    ) {
        reset(eventRepository);

        UUID generatedId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setId(generatedId);
            event.setCreatedDate(now);
            event.setLastModifiedDate(now);
            return event;
        });

        EventResponse response = eventService.createEvent(request, null);

        // Verify all metadata fields match the request
        assertThat(response.getId()).isEqualTo(generatedId);
        assertThat(response.getName()).isEqualTo(request.getName());
        assertThat(response.getDescription()).isEqualTo(request.getDescription());
        assertThat(response.getEventType()).isEqualTo(request.getEventType());
        assertThat(response.getStatus()).isEqualTo(EventStatus.DRAFT.name());
        assertThat(response.getLocation()).isEqualTo(request.getLocation());
        assertThat(response.getCountry()).isEqualTo(request.getCountry());
        assertThat(response.getState()).isEqualTo(request.getState());
        assertThat(response.getStartDate()).isEqualTo(request.getStartDate());
        assertThat(response.getEndDate()).isEqualTo(request.getEndDate());
        assertThat(response.getStartTime()).isEqualTo(request.getStartTime());
        assertThat(response.getEndTime()).isEqualTo(request.getEndTime());
        assertThat(response.getRegistrationDeadline()).isEqualTo(request.getRegistrationDeadline());
        assertThat(response.getCapacity()).isEqualTo(request.getCapacity());
        assertThat(response.getCurrentRegistrationCount()).isEqualTo(0);

        // imagePath should be null when no image is provided
        assertThat(response.getImagePath()).isNull();

        verify(eventRepository).save(any(Event.class));
    }

    // ==================== Property: Update Without Image Preserves Fields ====================

    /**
     * For all valid update metadata on DRAFT events without image: updateEvent(id, request)
     * updates only provided fields, imagePath unchanged.
     *
     * <p><b>Validates: Requirements 3.2</b></p>
     */
    @Property(tries = 50)
    void updateEventOnDraftWithoutImage_updatesOnlyProvidedFields(
            @ForAll("validUpdateRequests") UpdateEventRequest request
    ) {
        reset(eventRepository);

        UUID eventId = UUID.randomUUID();
        String existingImagePath = null; // no image on the existing event

        Event existingEvent = Event.builder()
                .id(eventId)
                .name("Original Name")
                .description("Original Description")
                .eventType("Showcase")
                .status(EventStatus.DRAFT)
                .location("Original Location")
                .country("Nigeria")
                .state("Lagos")
                .startDate(LocalDate.now().plusDays(10))
                .endDate(LocalDate.now().plusDays(11))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .capacity(100)
                .currentRegistrationCount(0)
                .imagePath(existingImagePath)
                .createdDate(LocalDateTime.now().minusDays(1))
                .lastModifiedDate(LocalDateTime.now().minusDays(1))
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setLastModifiedDate(LocalDateTime.now());
            return event;
        });

        EventResponse response = eventService.updateEvent(eventId, request, null);

        // Verify provided fields are updated
        if (request.getName() != null) {
            assertThat(response.getName()).isEqualTo(request.getName());
        }
        if (request.getDescription() != null) {
            assertThat(response.getDescription()).isEqualTo(request.getDescription());
        }
        if (request.getEventType() != null) {
            assertThat(response.getEventType()).isEqualTo(request.getEventType());
        }
        if (request.getLocation() != null) {
            assertThat(response.getLocation()).isEqualTo(request.getLocation());
        }
        if (request.getCountry() != null) {
            assertThat(response.getCountry()).isEqualTo(request.getCountry());
        }
        if (request.getCapacity() != null) {
            assertThat(response.getCapacity()).isEqualTo(request.getCapacity());
        }

        // imagePath should remain unchanged (null)
        assertThat(response.getImagePath()).isNull();
    }

    // ==================== Property: Delete Non-DRAFT Events Throws IllegalStateException ====================

    /**
     * For all non-DRAFT events: deleteEvent(id) throws IllegalStateException.
     *
     * <p><b>Validates: Requirements 3.3</b></p>
     */
    @Property(tries = 20)
    void deleteEvent_onNonDraftEvent_throwsIllegalStateException(
            @ForAll("nonDraftStatuses") EventStatus status
    ) {
        reset(eventRepository);

        UUID eventId = UUID.randomUUID();
        Event event = Event.builder()
                .id(eventId)
                .name("Test Event")
                .eventType("Combine")
                .status(status)
                .location("Abuja")
                .country("Nigeria")
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(6))
                .capacity(50)
                .currentRegistrationCount(0)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.deleteEvent(eventId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DRAFT");

        // Verify delete was never called on the repository
        verify(eventRepository, never()).delete(any(Event.class));
    }

    // ==================== Property: Invalid Status Transitions Throw IllegalStateException ====================

    /**
     * For all invalid status transitions: transitionStatus(id, newStatus) throws IllegalStateException.
     *
     * <p><b>Validates: Requirements 3.4</b></p>
     */
    @Property(tries = 50)
    void transitionStatus_invalidTransition_throwsIllegalStateException(
            @ForAll("invalidTransitions") StatusTransition transition
    ) {
        reset(eventRepository);

        UUID eventId = UUID.randomUUID();
        Event event = Event.builder()
                .id(eventId)
                .name("Test Event")
                .eventType("Showcase")
                .status(transition.from())
                .location("Lagos")
                .country("Nigeria")
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(6))
                .capacity(100)
                .currentRegistrationCount(0)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.transitionStatus(eventId, transition.to()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transition");

        // Verify save was never called (no state change persisted)
        verify(eventRepository, never()).save(any(Event.class));
    }

    // ==================== Property: Valid Status Transitions Succeed ====================

    /**
     * For all valid status transitions: transitionStatus(id, newStatus) succeeds and returns updated status.
     *
     * <p><b>Validates: Requirements 3.5</b></p>
     */
    @Property(tries = 20)
    void transitionStatus_validTransition_succeedsAndReturnsUpdatedStatus(
            @ForAll("validTransitions") StatusTransition transition
    ) {
        reset(eventRepository);

        UUID eventId = UUID.randomUUID();
        Event event = Event.builder()
                .id(eventId)
                .name("Test Event")
                .eventType("Showcase")
                .status(transition.from())
                .location("Lagos")
                .country("Nigeria")
                .startDate(LocalDate.now().plusDays(5))
                .endDate(LocalDate.now().plusDays(6))
                .capacity(100)
                .currentRegistrationCount(0)
                .createdDate(LocalDateTime.now())
                .lastModifiedDate(LocalDateTime.now())
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EventResponse response = eventService.transitionStatus(eventId, transition.to());

        assertThat(response.getStatus()).isEqualTo(transition.to().name());
        verify(eventRepository).save(any(Event.class));
    }

    // ==================== Providers ====================

    @Provide
    Arbitrary<CreateEventRequest> validCreateRequests() {
        Arbitrary<String> names = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withChars(' ')
                .ofMinLength(3)
                .ofMaxLength(50);

        Arbitrary<String> descriptions = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withChars(' ', '.', ',')
                .ofMinLength(0)
                .ofMaxLength(200);

        Arbitrary<String> eventTypes = Arbitraries.of("Showcase", "Combine", "Camp", "Tryout", "Evaluation");

        Arbitrary<String> locations = Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withChars(' ')
                .ofMinLength(3)
                .ofMaxLength(50);

        Arbitrary<String> countries = Arbitraries.of("Nigeria", "Ghana", "Kenya", "South Africa", "Cameroon");

        Arbitrary<String> states = Arbitraries.of("Lagos", "Abuja", "Rivers", "Kaduna", "Ogun", null);

        Arbitrary<Integer> capacities = Arbitraries.integers().between(1, 10000);

        Arbitrary<Integer> dayOffsets = Arbitraries.integers().between(1, 365);

        return Combinators.combine(names, descriptions, eventTypes, locations, countries, states, capacities, dayOffsets)
                .as((name, desc, type, location, country, state, capacity, dayOffset) ->
                        CreateEventRequest.builder()
                                .name(name)
                                .description(desc)
                                .eventType(type)
                                .location(location)
                                .country(country)
                                .state(state)
                                .startDate(LocalDate.now().plusDays(dayOffset))
                                .endDate(LocalDate.now().plusDays(dayOffset + 1))
                                .startTime(LocalTime.of(9, 0))
                                .endTime(LocalTime.of(17, 0))
                                .capacity(capacity)
                                .build()
                );
    }

    @Provide
    Arbitrary<UpdateEventRequest> validUpdateRequests() {
        Arbitrary<String> names = Arbitraries.of(
                "Updated Event A", "New Combine Name", "Showcase 2025", null);

        Arbitrary<String> descriptions = Arbitraries.of(
                "Updated description", "A longer description for the event", null);

        Arbitrary<String> eventTypes = Arbitraries.of("Showcase", "Combine", "Camp", null);

        Arbitrary<String> locations = Arbitraries.of("New Stadium", "Training Ground", null);

        Arbitrary<String> countries = Arbitraries.of("Nigeria", "Ghana", null);

        Arbitrary<Integer> capacities = Arbitraries.of(50, 100, 500, 1000, null);

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
    Arbitrary<EventStatus> nonDraftStatuses() {
        return Arbitraries.of(
                EventStatus.REGISTRATION_OPEN,
                EventStatus.REGISTRATION_CLOSED,
                EventStatus.IN_PROGRESS,
                EventStatus.COMPLETED,
                EventStatus.CANCELLED
        );
    }

    @Provide
    Arbitrary<StatusTransition> invalidTransitions() {
        List<StatusTransition> invalid = new ArrayList<>();

        for (EventStatus from : EventStatus.values()) {
            Set<EventStatus> allowed = VALID_TRANSITIONS.getOrDefault(from, Set.of());
            for (EventStatus to : EventStatus.values()) {
                if (!allowed.contains(to) && from != to) {
                    invalid.add(new StatusTransition(from, to));
                }
            }
        }

        return Arbitraries.of(invalid);
    }

    @Provide
    Arbitrary<StatusTransition> validTransitions() {
        List<StatusTransition> valid = new ArrayList<>();

        for (EventStatus from : EventStatus.values()) {
            Set<EventStatus> allowed = VALID_TRANSITIONS.getOrDefault(from, Set.of());
            for (EventStatus to : allowed) {
                valid.add(new StatusTransition(from, to));
            }
        }

        return Arbitraries.of(valid);
    }

    // ==================== Helper Record ====================

    record StatusTransition(EventStatus from, EventStatus to) {}
}
