package com.feadev.eaids.camp.event;

import com.feadev.eaids.camp.model.enums.CampStatus;
import com.feadev.eaids.registrations.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CampStatusChangedEvent extends BaseDomainEvent {

    private final UUID campId;
    private final CampStatus previousStatus;
    private final CampStatus newStatus;

    public CampStatusChangedEvent(UUID campId, CampStatus previousStatus, CampStatus newStatus) {
        this.campId = campId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
    }
}
