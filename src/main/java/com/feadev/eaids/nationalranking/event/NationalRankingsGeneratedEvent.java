package com.feadev.eaids.nationalranking.event;

import com.feadev.eaids.registrations.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class NationalRankingsGeneratedEvent extends BaseDomainEvent {

    private final UUID configId;
    private final int totalRankingsGenerated;

    public NationalRankingsGeneratedEvent(UUID configId, int totalRankingsGenerated) {
        this.configId = configId;
        this.totalRankingsGenerated = totalRankingsGenerated;
    }
}
