package com.feadev.eaids.registrations.event;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class BaseDomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final Instant timestamp = Instant.now();
}
