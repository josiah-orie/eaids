package com.feadev.eaids.academy.event;

import com.feadev.eaids.registrations.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

/**
 * Published after an institution academy application is approved.
 */
@Getter
public class InstitutionApplicationApprovedEvent extends BaseDomainEvent {

    private final UUID applicationId;
    private final String contactEmail;
    private final String institutionName;

    public InstitutionApplicationApprovedEvent(UUID applicationId, String contactEmail,
                                               String institutionName) {
        this.applicationId = applicationId;
        this.contactEmail = contactEmail;
        this.institutionName = institutionName;
    }
}
