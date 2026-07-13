package com.feadev.eaids.ranking.model;

import com.feadev.eaids.enrollment.model.CampEnrollment;
import com.feadev.eaids.ranking.model.enums.RankingType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "athlete_rankings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AthleteRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private CampEnrollment enrollment;

    @Column(name = "camp_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID campId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ranking_type", nullable = false)
    private RankingType rankingType;

    @Column(name = "rank_value", nullable = false)
    private int rankValue;

    @Column(name = "total_in_category", nullable = false)
    private int totalInCategory;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal percentile;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "filter_value", length = 100)
    private String filterValue;

    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
}
