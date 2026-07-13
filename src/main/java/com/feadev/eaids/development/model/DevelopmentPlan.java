package com.feadev.eaids.development.model;

import com.feadev.eaids.development.model.enums.DevelopmentPlanStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "development_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DevelopmentPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "athlete_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID athleteUserId;

    @Column(name = "camp_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID campId;

    @Column(name = "coach_user_id", columnDefinition = "BINARY(16)")
    private UUID coachUserId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DevelopmentPlanStatus status = DevelopmentPlanStatus.DRAFT;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "target_end_date")
    private LocalDate targetEndDate;

    @Column(name = "overall_assessment", columnDefinition = "TEXT")
    private String overallAssessment;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DevelopmentStrength> strengths = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DevelopmentWeakness> weaknesses = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DevelopmentGoal> goals = new ArrayList<>();

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DevelopmentProgress> progressEntries = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;
}
