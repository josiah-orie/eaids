package com.feadev.eaids.academy.model;

import com.feadev.eaids.academy.model.enums.ApplicantType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Join entity enrolling an approved applicant (individual or institution) into a training schedule.
 */
@Entity
@Table(name = "academy_schedule_enrollments", uniqueConstraints = {
        @UniqueConstraint(name = "uk_academy_enrollment_schedule_individual",
                columnNames = {"schedule_id", "individual_application_id"}),
        @UniqueConstraint(name = "uk_academy_enrollment_schedule_institution",
                columnNames = {"schedule_id", "institution_application_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AcademyScheduleEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private AcademyTrainingSchedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "applicant_type", nullable = false)
    private ApplicantType applicantType;

    @Column(name = "individual_application_id", columnDefinition = "BINARY(16)")
    private UUID individualApplicationId;

    @Column(name = "institution_application_id", columnDefinition = "BINARY(16)")
    private UUID institutionApplicationId;

    @CreatedDate
    @Column(name = "enrolled_date", nullable = false, updatable = false)
    private LocalDateTime enrolledDate;
}
