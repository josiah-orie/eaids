package com.feadev.eaids.enrollment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {

    private UUID id;
    private UUID campId;
    private String campName;
    private UUID userId;
    private String athleteName;
    private String status;
    private int enrollmentNumber;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
