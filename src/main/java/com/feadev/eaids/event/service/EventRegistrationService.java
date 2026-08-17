package com.feadev.eaids.event.service;

import com.feadev.eaids.event.dto.request.GuestRegistrationRequest;
import com.feadev.eaids.event.dto.response.EventRegistrationResponse;
import com.feadev.eaids.event.model.enums.RegistrationStatus;

import java.util.List;
import java.util.UUID;

public interface EventRegistrationService {

    EventRegistrationResponse registerForEvent(UUID eventId, UUID userId);

    EventRegistrationResponse registerGuestForEvent(UUID eventId, GuestRegistrationRequest request);

    EventRegistrationResponse cancelRegistration(UUID eventId, UUID userId);

    EventRegistrationResponse cancelGuestRegistration(UUID eventId, String guestEmail);

    EventRegistrationResponse updateRegistrationStatus(UUID eventId, UUID registrationId, RegistrationStatus newStatus);

    List<EventRegistrationResponse> getEventRegistrations(UUID eventId);

    List<EventRegistrationResponse> getMyRegistrations(UUID userId);
}
