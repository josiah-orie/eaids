package com.feadev.eaids.event.service.impl;

import com.feadev.eaids.event.dto.request.CreateEventRequest;
import com.feadev.eaids.event.dto.request.UpdateEventRequest;
import com.feadev.eaids.event.dto.response.EventResponse;
import com.feadev.eaids.event.model.Event;
import com.feadev.eaids.event.model.enums.EventStatus;
import com.feadev.eaids.event.repository.EventRepository;
import com.feadev.eaids.event.repository.EventSpecification;
import com.feadev.eaids.event.service.EventService;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final StorageService storageService;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/jpg", "image/png");

    /**
     * Valid status transitions for event lifecycle.
     * CANCELLED can be reached from any non-terminal state.
     */
    private static final Map<EventStatus, Set<EventStatus>> VALID_TRANSITIONS = Map.of(
            EventStatus.DRAFT, Set.of(EventStatus.REGISTRATION_OPEN, EventStatus.CANCELLED),
            EventStatus.REGISTRATION_OPEN, Set.of(EventStatus.REGISTRATION_CLOSED, EventStatus.CANCELLED),
            EventStatus.REGISTRATION_CLOSED, Set.of(EventStatus.IN_PROGRESS, EventStatus.CANCELLED),
            EventStatus.IN_PROGRESS, Set.of(EventStatus.COMPLETED, EventStatus.CANCELLED),
            EventStatus.COMPLETED, Set.of(),
            EventStatus.CANCELLED, Set.of()
    );

    @Override
    public EventResponse createEvent(CreateEventRequest request, MultipartFile image) {
        Event event = Event.builder()
                .name(request.getName())
                .description(request.getDescription())
                .eventType(request.getEventType())
                .status(EventStatus.DRAFT)
                .location(request.getLocation())
                .country(request.getCountry())
                .state(request.getState())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .registrationDeadline(request.getRegistrationDeadline())
                .capacity(request.getCapacity())
                .currentRegistrationCount(0)
                .build();

        Event saved = eventRepository.save(event);

        if (image != null && !image.isEmpty()) {
            validateImageFile(image);
            String storedPath = storageService.store(image, "events/" + saved.getId() + "/image");
            saved.setImagePath(buildAbsoluteImageUrl(storedPath));
            saved.setEventImage(image.getOriginalFilename());
            saved = eventRepository.save(saved);
        }

        log.info("Event created: {} ({})", saved.getName(), saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEvent(UUID eventId) {
        Event event = findEventOrThrow(eventId);
        return mapToResponse(event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventResponse> getEventsByStatus(EventStatus status) {
        return eventRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EventResponse> getEvents(EventStatus status, String eventType, String country,
                                         String search, LocalDate startDateFrom, LocalDate startDateTo,
                                         Pageable pageable) {
        Specification<Event> spec = EventSpecification.withFilters(status, eventType, country,
                search, startDateFrom, startDateTo);
        return eventRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    public EventResponse updateEvent(UUID eventId, UpdateEventRequest request, MultipartFile image) {
        Event event = findEventOrThrow(eventId);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new IllegalStateException("Event can only be updated in DRAFT status");
        }

        if (request.getName() != null) {
            event.setName(request.getName());
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventType() != null) {
            event.setEventType(request.getEventType());
        }
        if (request.getLocation() != null) {
            event.setLocation(request.getLocation());
        }
        if (request.getCountry() != null) {
            event.setCountry(request.getCountry());
        }
        if (request.getState() != null) {
            event.setState(request.getState());
        }
        if (request.getStartDate() != null) {
            event.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            event.setEndDate(request.getEndDate());
        }
        if (request.getStartTime() != null) {
            event.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            event.setEndTime(request.getEndTime());
        }
        if (request.getRegistrationDeadline() != null) {
            event.setRegistrationDeadline(request.getRegistrationDeadline());
        }
        if (request.getCapacity() != null) {
            event.setCapacity(request.getCapacity());
        }

        // Handle image upload
        if (image != null && !image.isEmpty()) {
            validateImageFile(image);
            if (event.getImagePath() != null) {
                storageService.delete(extractRelativePath(event.getImagePath()));
            }
            String storedPath = storageService.store(image, "events/" + event.getId() + "/image");
            event.setImagePath(buildAbsoluteImageUrl(storedPath));
            event.setEventImage(image.getOriginalFilename());
        }

        Event saved = eventRepository.save(event);
        log.info("Event updated: {} ({})", saved.getName(), saved.getId());
        return mapToResponse(saved);
    }

    @Override
    public void deleteEvent(UUID eventId) {
        Event event = findEventOrThrow(eventId);

        if (event.getStatus() != EventStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT events can be deleted");
        }

        eventRepository.delete(event);
        log.info("Event deleted: {}", eventId);
    }

    @Override
    public EventResponse transitionStatus(UUID eventId, EventStatus newStatus) {
        Event event = findEventOrThrow(eventId);
        EventStatus currentStatus = event.getStatus();

        Set<EventStatus> allowedTransitions = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedTransitions.contains(newStatus)) {
            throw new IllegalStateException(
                    String.format("Cannot transition event from %s to %s. Allowed transitions: %s",
                            currentStatus, newStatus, allowedTransitions));
        }

        event.setStatus(newStatus);
        Event saved = eventRepository.save(event);
        log.info("Event {} transitioned from {} to {}", eventId, currentStatus, newStatus);

        return mapToResponse(saved);
    }

    private Event findEventOrThrow(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Image file is empty");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Image file exceeds 5MB limit");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid image type. Allowed: JPEG, PNG");
        }
    }

    private EventResponse mapToResponse(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .status(event.getStatus().name())
                .location(event.getLocation())
                .country(event.getCountry())
                .state(event.getState())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .registrationDeadline(event.getRegistrationDeadline())
                .capacity(event.getCapacity())
                .currentRegistrationCount(event.getCurrentRegistrationCount())
                .eventImage(event.getEventImage())
                .imagePath(event.getImagePath())
                .createdDate(event.getCreatedDate())
                .lastModifiedDate(event.getLastModifiedDate())
                .createdBy(event.getCreatedBy())
                .lastModifiedBy(event.getLastModifiedBy())
                .build();
    }

    private String buildAbsoluteImageUrl(String relativePath) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String path = relativePath.startsWith("/") ? relativePath.substring(1) : relativePath;
        return base + "/storage/" + path;
    }

    private String extractRelativePath(String absoluteUrl) {
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String storagePrefix = base + "storage/";
        if (absoluteUrl.startsWith(storagePrefix)) {
            return absoluteUrl.substring(storagePrefix.length());
        }
        if (absoluteUrl.startsWith(base)) {
            return absoluteUrl.substring(base.length());
        }
        return absoluteUrl; // fallback: return as-is if not prefixed
    }
}
