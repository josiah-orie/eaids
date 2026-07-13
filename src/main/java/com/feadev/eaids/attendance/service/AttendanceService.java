package com.feadev.eaids.attendance.service;

import com.feadev.eaids.attendance.dto.request.BatchAttendanceRequest;
import com.feadev.eaids.attendance.dto.response.AttendanceResponse;
import com.feadev.eaids.attendance.dto.response.AttendanceSummaryResponse;

import java.util.List;
import java.util.UUID;

public interface AttendanceService {

    List<AttendanceResponse> recordBatchAttendance(UUID campId, BatchAttendanceRequest request, String recordedBy);

    List<AttendanceResponse> getAttendanceByEnrollment(UUID enrollmentId);

    AttendanceSummaryResponse getAttendanceSummary(UUID enrollmentId);
}
