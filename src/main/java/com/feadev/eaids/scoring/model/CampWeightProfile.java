package com.feadev.eaids.scoring.model;

import com.feadev.eaids.camp.model.Camp;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "camp_weight_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_weight_profile_camp_position", columnNames = {"camp_id", "position"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CampWeightProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camp_id", nullable = false)
    private Camp camp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FootballPosition position;

    @Builder.Default
    @Column(name = "physical_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal physicalWeight = new BigDecimal("20.00");

    @Builder.Default
    @Column(name = "technical_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal technicalWeight = new BigDecimal("25.00");

    @Builder.Default
    @Column(name = "tactical_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal tacticalWeight = new BigDecimal("20.00");

    @Builder.Default
    @Column(name = "attendance_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal attendanceWeight = new BigDecimal("5.00");

    @Builder.Default
    @Column(name = "discipline_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal disciplineWeight = new BigDecimal("5.00");

    @Builder.Default
    @Column(name = "coach_review_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal coachReviewWeight = new BigDecimal("25.00");

    @Builder.Default
    @Column(name = "match_stats_weight", nullable = false, precision = 5, scale = 2)
    private BigDecimal matchStatsWeight = new BigDecimal("0.00");

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;
}
