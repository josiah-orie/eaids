package com.feadev.eaids.academy.event;

import com.feadev.eaids.registrations.event.BaseDomainEvent;
import lombok.Getter;

import java.util.UUID;

/**
 * Published after an approved applicant is enrolled into a training schedule.
 */
@Getter
public class ApplicantEnrolledEvent extends BaseDomainEvent {

    private final UUID scheduleId;
    private final String applicantEmail;
    private final String applicantName;
    private final String scheduleTitle;
    private final String startDate;

    public ApplicantEnrolledEvent(UUID scheduleId, String applicantEmail, String applicantName,
                                  String scheduleTitle, String startDate) {
        this.scheduleId = scheduleId;
        this.applicantEmail = applicantEmail;
        this.applicantName = applicantName;
        this.scheduleTitle = scheduleTitle;
        this.startDate = startDate;
    }
}
