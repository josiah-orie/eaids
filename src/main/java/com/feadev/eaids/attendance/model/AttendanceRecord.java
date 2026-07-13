package com.feadev.eaids.attendance.model;

import com.feadev.eaids.attendance.model.enums.AttendanceStatus;
import com.feadev.eaids.attendance.model.enums.SessionType;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attendance_records", uniqueConstraints = {
        @UniqueConstraint(name = "uk_attendance_enrollment_date_session",
                columnNames = {"enrollment_id", "attendance_date", "session_type"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private CampEnrollment enrollment;

    @Column(name = "camp_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID campId;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private SessionType sessionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;

    @Column(name = "score", precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "recorded_by")
    private String recordedBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
}
