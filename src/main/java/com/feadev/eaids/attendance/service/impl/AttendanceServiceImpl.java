package com.feadev.eaids.attendance.service.impl;

import com.feadev.eaids.attendance.dto.request.BatchAttendanceRequest;
import com.feadev.eaids.attendance.dto.response.AttendanceResponse;
import com.feadev.eaids.attendance.dto.response.AttendanceSummaryResponse;
import com.feadev.eaids.attendance.model.AttendanceRecord;
import com.feadev.eaids.attendance.model.enums.AttendanceStatus;
import com.feadev.eaids.attendance.repository.AttendanceRecordRepository;
import com.feadev.eaids.attendance.service.AttendanceService;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.enrollment.repository.CampEnrollmentRepository;
import com.feadev.eaids.registrations.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRecordRepository attendanceRepository;
    private final CampEnrollmentRepository enrollmentRepository;

    @Override
    public List<AttendanceResponse> recordBatchAttendance(UUID campId, BatchAttendanceRequest request, String recordedBy) {
        List<AttendanceRecord> records = new ArrayList<>();

        for (BatchAttendanceRequest.AttendanceEntry entry : request.getEntries()) {
            CampEnrollment enrollment = enrollmentRepository.findById(entry.getEnrollmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Enrollment not found: " + entry.getEnrollmentId()));

            if (!enrollment.getCamp().getId().equals(campId)) {
                throw new IllegalStateException("Enrollment does not belong to this camp");
            }

            BigDecimal score = calculateAttendanceScore(entry.getStatus());

            AttendanceRecord record = AttendanceRecord.builder()
                    .enrollment(enrollment)
                    .campId(campId)
                    .attendanceDate(request.getAttendanceDate())
                    .sessionType(request.getSessionType())
                    .status(entry.getStatus())
                    .score(score)
                    .recordedBy(recordedBy)
                    .build();

            records.add(record);
        }

        List<AttendanceRecord> saved = attendanceRepository.saveAll(records);
        log.info("Batch attendance recorded for camp {} - {} entries", campId, saved.size());

        return saved.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceByEnrollment(UUID enrollmentId) {
        return attendanceRepository.findByEnrollmentId(enrollmentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceSummaryResponse getAttendanceSummary(UUID enrollmentId) {
        List<AttendanceRecord> records = attendanceRepository.findByEnrollmentId(enrollmentId);

        if (records.isEmpty()) {
            CampEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + enrollmentId));
            return AttendanceSummaryResponse.builder()
                    .enrollmentId(enrollmentId)
                    .campId(enrollment.getCamp().getId())
                    .totalSessions(0)
                    .presentCount(0)
                    .lateCount(0)
                    .excusedCount(0)
                    .absentCount(0)
                    .averageScore(BigDecimal.ZERO)
                    .build();
        }

        int present = 0, late = 0, excused = 0, absent = 0;
        for (AttendanceRecord record : records) {
            switch (record.getStatus()) {
                case PRESENT -> present++;
                case LATE -> late++;
                case EXCUSED -> excused++;
                case ABSENT -> absent++;
            }
        }

        BigDecimal avgScore = attendanceRepository.calculateAverageScoreByEnrollmentId(enrollmentId);
        if (avgScore == null) {
            avgScore = BigDecimal.ZERO;
        }

        return AttendanceSummaryResponse.builder()
                .enrollmentId(enrollmentId)
                .campId(records.getFirst().getCampId())
                .totalSessions(records.size())
                .presentCount(present)
                .lateCount(late)
                .excusedCount(excused)
                .absentCount(absent)
                .averageScore(avgScore.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    /**
     * Score calculation: PRESENT=100%, LATE=75%, EXCUSED=50%, ABSENT=0%
     */
    private BigDecimal calculateAttendanceScore(AttendanceStatus status) {
        return switch (status) {
            case PRESENT -> new BigDecimal("100.00");
            case LATE -> new BigDecimal("75.00");
            case EXCUSED -> new BigDecimal("50.00");
            case ABSENT -> BigDecimal.ZERO;
        };
    }

    private AttendanceResponse mapToResponse(AttendanceRecord record) {
        return AttendanceResponse.builder()
                .id(record.getId())
                .enrollmentId(record.getEnrollment().getId())
                .campId(record.getCampId())
                .attendanceDate(record.getAttendanceDate())
                .sessionType(record.getSessionType().name())
                .status(record.getStatus().name())
                .score(record.getScore())
                .recordedBy(record.getRecordedBy())
                .createdDate(record.getCreatedDate())
                .build();
    }
}
