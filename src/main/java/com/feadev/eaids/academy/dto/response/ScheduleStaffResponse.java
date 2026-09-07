package com.feadev.eaids.academy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API representation of a staff assignment on a training schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleStaffResponse {

    private UUID id;
    private UUID scheduleId;
    private String staffType;
    private UUID profileId;
    private String staffName;
    private String staffEmail;
    private LocalDateTime assignedDate;
}
