package com.feadev.eaids.ranking.event;

import com.feadev.eaids.registrations.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class RankingsGeneratedEvent extends BaseDomainEvent {

    private final UUID campId;

    public RankingsGeneratedEvent(UUID campId) {
        this.campId = campId;
    }
}
