package com.feadev.eaids.academy.model;

import com.feadev.eaids.academy.model.enums.StaffType;
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
 * Join entity assigning a coach or scout (by their profile id) to a training schedule.
 */
@Entity
@Table(name = "academy_schedule_staff", uniqueConstraints = {
        @UniqueConstraint(name = "uk_academy_staff_schedule_coach",
                columnNames = {"schedule_id", "coach_profile_id"}),
        @UniqueConstraint(name = "uk_academy_staff_schedule_scout",
                columnNames = {"schedule_id", "scout_profile_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AcademyScheduleStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private AcademyTrainingSchedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "staff_type", nullable = false)
    private StaffType staffType;

    @Column(name = "coach_profile_id", columnDefinition = "BINARY(16)")
    private UUID coachProfileId;

    @Column(name = "scout_profile_id", columnDefinition = "BINARY(16)")
    private UUID scoutProfileId;

    @CreatedDate
    @Column(name = "assigned_date", nullable = false, updatable = false)
    private LocalDateTime assignedDate;
}
