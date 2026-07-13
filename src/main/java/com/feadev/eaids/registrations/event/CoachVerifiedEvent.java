package com.feadev.eaids.registrations.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CoachVerifiedEvent extends BaseDomainEvent {
    private final UUID coachId;
    private final String email;
}
