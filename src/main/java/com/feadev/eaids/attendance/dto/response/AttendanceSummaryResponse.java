package com.feadev.eaids.attendance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceSummaryResponse {

    private UUID enrollmentId;
    private UUID campId;
    private int totalSessions;
    private int presentCount;
    private int lateCount;
    private int excusedCount;
    private int absentCount;
    private BigDecimal averageScore;
}
