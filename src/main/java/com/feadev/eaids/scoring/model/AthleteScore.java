package com.feadev.eaids.scoring.model;

import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "athlete_scores", uniqueConstraints = {
        @UniqueConstraint(name = "uk_athlete_score_enrollment", columnNames = {"enrollment_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AthleteScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private CampEnrollment enrollment;

    @Column(name = "camp_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID campId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FootballPosition position;

    @Column(name = "physical_score", precision = 5, scale = 2)
    private BigDecimal physicalScore;

    @Column(name = "technical_score", precision = 5, scale = 2)
    private BigDecimal technicalScore;

    @Column(name = "tactical_score", precision = 5, scale = 2)
    private BigDecimal tacticalScore;

    @Column(name = "attendance_score", precision = 5, scale = 2)
    private BigDecimal attendanceScore;

    @Column(name = "discipline_score", precision = 5, scale = 2)
    private BigDecimal disciplineScore;

    @Column(name = "coach_review_score", precision = 5, scale = 2)
    private BigDecimal coachReviewScore;

    @Column(name = "match_stats_score", precision = 5, scale = 2)
    private BigDecimal matchStatsScore;

    @Column(name = "total_weighted_score", precision = 5, scale = 2)
    private BigDecimal totalWeightedScore;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
}
