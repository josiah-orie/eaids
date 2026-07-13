package com.feadev.eaids.scouting.event;

import com.feadev.eaids.registrations.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class InterviewRequestRejectedEvent extends BaseDomainEvent {

    private final UUID interviewRequestId;
    private final UUID scoutUserId;
    private final UUID athleteUserId;

    public InterviewRequestRejectedEvent(UUID interviewRequestId, UUID scoutUserId, UUID athleteUserId) {
        this.interviewRequestId = interviewRequestId;
        this.scoutUserId = scoutUserId;
        this.athleteUserId = athleteUserId;
    }
}
