package com.feadev.eaids.academy.event;

import com.feadev.eaids.registrations.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

/**
 * Published after a coach or scout is assigned to a training schedule.
 */
@Getter
public class ScheduleStaffAssignedEvent extends BaseDomainEvent {

    private final UUID scheduleId;
    private final String staffEmail;
    private final String staffName;
    private final String scheduleTitle;

    public ScheduleStaffAssignedEvent(UUID scheduleId, String staffEmail,
                                      String staffName, String scheduleTitle) {
        this.scheduleId = scheduleId;
        this.staffEmail = staffEmail;
        this.staffName = staffName;
        this.scheduleTitle = scheduleTitle;
    }
}
