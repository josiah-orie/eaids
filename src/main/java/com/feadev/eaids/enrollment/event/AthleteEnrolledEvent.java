package com.feadev.eaids.enrollment.event;

import com.feadev.eaids.enrollment.model.enums.EnrollmentStatus;
import com.feadev.eaids.registrations.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

@Getter
public class AthleteEnrolledEvent extends BaseDomainEvent {

    private final UUID enrollmentId;
    private final UUID campId;
    private final UUID athleteUserId;
    private final EnrollmentStatus status;

    public AthleteEnrolledEvent(UUID enrollmentId, UUID campId, UUID athleteUserId, EnrollmentStatus status) {
        this.enrollmentId = enrollmentId;
        this.campId = campId;
        this.athleteUserId = athleteUserId;
        this.status = status;
    }
}
