package com.feadev.eaids.event.service.impl;

import com.feadev.eaids.event.dto.request.GuestRegistrationRequest;
import com.feadev.eaids.event.dto.response.EventRegistrationResponse;
import com.feadev.eaids.event.model.Event;
import com.feadev.eaids.event.model.EventRegistration;
import com.feadev.eaids.event.model.enums.EventStatus;
import com.feadev.eaids.event.model.enums.RegistrationStatus;
import com.feadev.eaids.event.repository.EventRegistrationRepository;
import com.feadev.eaids.event.repository.EventRepository;
import com.feadev.eaids.event.service.EventRegistrationService;
import com.feadev.eaids.registrations.exception.DuplicateResourceException;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import com.feadev.eaids.registrations.model.User;
import com.feadev.eaids.registrations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventRegistrationServiceImpl implements EventRegistrationService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final UserRepository userRepository;

    @Override
    public EventRegistrationResponse registerForEvent(UUID eventId, UUID userId) {
        Event event = findEventOrThrow(eventId);
        User user = findUserOrThrow(userId);

        validateEventOpenForRegistration(event);
        validateCapacity(event);

        // Check for duplicate registration
        if (eventRegistrationRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new DuplicateResourceException("User is already registered for this event");
        }

        // Create registration
        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .user(user)
                .status(RegistrationStatus.REGISTERED)
                .build();

        EventRegistration saved = eventRegistrationRepository.save(registration);

        // Increment registration count
        event.setCurrentRegistrationCount(event.getCurrentRegistrationCount() + 1);
        eventRepository.save(event);

        log.info("User {} registered for event {} ({})", userId, event.getName(), eventId);
        return mapToResponse(saved);
    }

    @Override
    public EventRegistrationResponse registerGuestForEvent(UUID eventId, GuestRegistrationRequest request) {
        Event event = findEventOrThrow(eventId);

        validateEventOpenForRegistration(event);
        validateCapacity(event);

        // Check for duplicate guest registration by email
        if (eventRegistrationRepository.existsByEventIdAndGuestEmail(eventId, request.getEmail())) {
            throw new DuplicateResourceException("A guest with this email is already registered for this event");
        }

        // Create guest registration (no user association)
        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .user(null)
                .guestName(request.getName())
                .guestEmail(request.getEmail())
                .guestPhone(request.getPhone())
                .status(RegistrationStatus.REGISTERED)
                .build();

        EventRegistration saved = eventRegistrationRepository.save(registration);

        // Increment registration count
        event.setCurrentRegistrationCount(event.getCurrentRegistrationCount() + 1);
        eventRepository.save(event);

        log.info("Guest '{}' ({}) registered for event {} ({})", request.getName(), request.getEmail(), event.getName(), eventId);
        return mapToResponse(saved);
    }

    @Override
    public EventRegistrationResponse cancelRegistration(UUID eventId, UUID userId) {
        Event event = findEventOrThrow(eventId);

        EventRegistration registration = eventRegistrationRepository.findByEventIdAndUserId(eventId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found for this user and event"));

        cancelAndDecrement(registration, event);

        log.info("User {} cancelled registration for event {}", userId, eventId);
        return mapToResponse(registration);
    }

    @Override
    public EventRegistrationResponse cancelGuestRegistration(UUID eventId, String guestEmail) {
        Event event = findEventOrThrow(eventId);

        EventRegistration registration = eventRegistrationRepository.findByEventIdAndGuestEmail(eventId, guestEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Guest registration not found for this email and event"));

        cancelAndDecrement(registration, event);

        log.info("Guest '{}' cancelled registration for event {}", guestEmail, eventId);
        return mapToResponse(registration);
    }

    @Override
    public EventRegistrationResponse updateRegistrationStatus(UUID eventId, UUID registrationId, RegistrationStatus newStatus) {
        findEventOrThrow(eventId);

        EventRegistration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found with id: " + registrationId));

        // Verify registration belongs to the event
        if (!registration.getEvent().getId().equals(eventId)) {
            throw new IllegalStateException("Registration does not belong to the specified event");
        }

        // Validate status transition
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update a cancelled registration");
        }

        registration.setStatus(newStatus);
        EventRegistration saved = eventRegistrationRepository.save(registration);

        log.info("Registration {} status updated to {} for event {}", registrationId, newStatus, eventId);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getEventRegistrations(UUID eventId) {
        findEventOrThrow(eventId);
        return eventRegistrationRepository.findByEventId(eventId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventRegistrationResponse> getMyRegistrations(UUID userId) {
        return eventRegistrationRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ======================== PRIVATE HELPERS ========================

    private void validateEventOpenForRegistration(Event event) {
        if (event.getStatus() != EventStatus.REGISTRATION_OPEN) {
            throw new IllegalStateException("Event is not open for registration. Current status: " + event.getStatus());
        }
    }

    private void validateCapacity(Event event) {
        if (event.getCurrentRegistrationCount() >= event.getCapacity()) {
            throw new IllegalStateException("Event has reached maximum capacity");
        }
    }

    private void cancelAndDecrement(EventRegistration registration, Event event) {
        Set<RegistrationStatus> cancellableStatuses = Set.of(RegistrationStatus.REGISTERED, RegistrationStatus.CONFIRMED);
        if (!cancellableStatuses.contains(registration.getStatus())) {
            throw new IllegalStateException(
                    "Cannot cancel registration with status: " + registration.getStatus() +
                            ". Only REGISTERED or CONFIRMED registrations can be cancelled.");
        }

        registration.setStatus(RegistrationStatus.CANCELLED);
        eventRegistrationRepository.save(registration);

        // Decrement registration count
        event.setCurrentRegistrationCount(Math.max(0, event.getCurrentRegistrationCount() - 1));
        eventRepository.save(event);
    }

    private Event findEventOrThrow(UUID eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
    }

    private User findUserOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private EventRegistrationResponse mapToResponse(EventRegistration registration) {
        EventRegistrationResponse.EventRegistrationResponseBuilder builder = EventRegistrationResponse.builder()
                .id(registration.getId())
                .eventId(registration.getEvent().getId())
                .eventName(registration.getEvent().getName())
                .guest(registration.isGuestRegistration())
                .status(registration.getStatus().name())
                .createdDate(registration.getCreatedDate())
                .lastModifiedDate(registration.getLastModifiedDate());

        if (registration.isGuestRegistration()) {
            builder.guestName(registration.getGuestName())
                    .guestEmail(registration.getGuestEmail())
                    .guestPhone(registration.getGuestPhone());
        } else {
            builder.userId(registration.getUser().getId())
                    .userEmail(registration.getUser().getEmail());
        }

        return builder.build();
    }
}
