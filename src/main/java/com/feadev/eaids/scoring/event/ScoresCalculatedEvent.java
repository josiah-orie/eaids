package com.feadev.eaids.scoring.event;

import com.feadev.eaids.registrations.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ScoresCalculatedEvent extends BaseDomainEvent {

    private final UUID campId;

    public ScoresCalculatedEvent(UUID campId) {
        this.campId = campId;
    }
}
