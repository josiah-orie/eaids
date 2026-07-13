package com.feadev.eaids.registrations.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class AthleteApprovedEvent extends BaseDomainEvent {
    private final UUID athleteId;
    private final String email;
}
