package com.feadev.eaids.scouting.event;

import com.feadev.eaids.registrations.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class InterviewRequestApprovedEvent extends BaseDomainEvent {

    private final UUID interviewRequestId;
    private final UUID scoutUserId;
    private final UUID athleteUserId;

    public InterviewRequestApprovedEvent(UUID interviewRequestId, UUID scoutUserId, UUID athleteUserId) {
        this.interviewRequestId = interviewRequestId;
        this.scoutUserId = scoutUserId;
        this.athleteUserId = athleteUserId;
    }
}
