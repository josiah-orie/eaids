package com.feadev.eaids.event.service;

import com.feadev.eaids.event.dto.request.CreateEventRequest;
import com.feadev.eaids.event.dto.request.UpdateEventRequest;
import com.feadev.eaids.event.dto.response.EventResponse;
import com.feadev.eaids.event.model.enums.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EventService {

    EventResponse createEvent(CreateEventRequest request, MultipartFile image);

    EventResponse getEvent(UUID eventId);

    List<EventResponse> getAllEvents();

    List<EventResponse> getEventsByStatus(EventStatus status);

    Page<EventResponse> getEvents(EventStatus status, String eventType, String country,
                                  String search, LocalDate startDateFrom, LocalDate startDateTo,
                                  Pageable pageable);

    EventResponse updateEvent(UUID eventId, UpdateEventRequest request, MultipartFile image);

    void deleteEvent(UUID eventId);

    EventResponse transitionStatus(UUID eventId, EventStatus newStatus);
}
