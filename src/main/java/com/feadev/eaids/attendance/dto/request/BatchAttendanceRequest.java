package com.feadev.eaids.attendance.dto.request;

import com.feadev.eaids.attendance.model.enums.AttendanceStatus;
import com.feadev.eaids.attendance.model.enums.SessionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAttendanceRequest {

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    @NotNull(message = "Session type is required")
    private SessionType sessionType;

    @NotEmpty(message = "At least one attendance entry is required")
    @Valid
    private List<AttendanceEntry> entries;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceEntry {

        @NotNull(message = "Enrollment ID is required")
        private UUID enrollmentId;

        @NotNull(message = "Attendance status is required")
        private AttendanceStatus status;
    }
}
