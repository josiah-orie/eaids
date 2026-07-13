package com.feadev.eaids.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {

    private UUID id;
    private UUID enrollmentId;
    private UUID campId;
    private LocalDate attendanceDate;
    private String sessionType;
    private String status;
    private BigDecimal score;
    private String recordedBy;
    private LocalDateTime createdDate;
}
