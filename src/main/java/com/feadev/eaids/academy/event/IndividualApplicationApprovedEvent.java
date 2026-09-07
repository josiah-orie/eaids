package com.feadev.eaids.academy.event;

import com.feadev.eaids.registrations.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

/**
 * Published after an individual academy application is approved.
 */
@Getter
public class IndividualApplicationApprovedEvent extends BaseDomainEvent {

    private final UUID applicationId;
    private final String email;
    private final String fullName;

    public IndividualApplicationApprovedEvent(UUID applicationId, String email, String fullName) {
        this.applicationId = applicationId;
        this.email = email;
        this.fullName = fullName;
    }
}
