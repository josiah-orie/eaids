package com.feadev.eaids.academy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API representation of an applicant enrollment on a training schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {

    private UUID id;
    private UUID scheduleId;
    private String applicantType;
    private UUID applicationId;
    private String applicantName;
    private String applicantEmail;
    private LocalDateTime enrolledDate;
}
