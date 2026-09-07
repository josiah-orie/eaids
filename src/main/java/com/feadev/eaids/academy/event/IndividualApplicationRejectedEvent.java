package com.feadev.eaids.academy.event;

import com.feadev.eaids.registrations.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

/**
 * Published after an individual academy application is rejected.
 */
@Getter
public class IndividualApplicationRejectedEvent extends BaseDomainEvent {

    private final UUID applicationId;
    private final String email;
    private final String fullName;
    private final String reason;

    public IndividualApplicationRejectedEvent(UUID applicationId, String email,
                                              String fullName, String reason) {
        this.applicationId = applicationId;
        this.email = email;
        this.fullName = fullName;
        this.reason = reason;
    }
}
