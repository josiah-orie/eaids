package com.feadev.eaids.selection.event;

import com.feadev.eaids.registrations.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class SelectionApprovedEvent extends BaseDomainEvent {

    private final UUID selectionId;
    private final UUID campId;
    private final UUID enrollmentId;

    public SelectionApprovedEvent(UUID selectionId, UUID campId, UUID enrollmentId) {
        this.selectionId = selectionId;
        this.campId = campId;
        this.enrollmentId = enrollmentId;
    }
}
