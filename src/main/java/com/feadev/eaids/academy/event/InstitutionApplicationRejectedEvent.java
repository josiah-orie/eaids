package com.feadev.eaids.academy.event;

import com.feadev.eaids.registrations.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

/**
 * Published after an institution academy application is rejected.
 */
@Getter
public class InstitutionApplicationRejectedEvent extends BaseDomainEvent {

    private final UUID applicationId;
    private final String contactEmail;
    private final String institutionName;
    private final String reason;

    public InstitutionApplicationRejectedEvent(UUID applicationId, String contactEmail,
                                               String institutionName, String reason) {
        this.applicationId = applicationId;
        this.contactEmail = contactEmail;
        this.institutionName = institutionName;
        this.reason = reason;
    }
}
