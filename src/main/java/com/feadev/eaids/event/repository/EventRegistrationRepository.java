package com.feadev.eaids.event.repository;

import com.feadev.eaids.event.model.EventRegistration;
import com.feadev.eaids.event.model.enums.RegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, UUID> {

    List<EventRegistration> findByEventId(UUID eventId);

    List<EventRegistration> findByEventIdAndStatus(UUID eventId, RegistrationStatus status);

    Optional<EventRegistration> findByEventIdAndUserId(UUID eventId, UUID userId);

    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);

    boolean existsByEventIdAndGuestEmail(UUID eventId, String guestEmail);

    Optional<EventRegistration> findByEventIdAndGuestEmail(UUID eventId, String guestEmail);

    List<EventRegistration> findByUserId(UUID userId);
}
