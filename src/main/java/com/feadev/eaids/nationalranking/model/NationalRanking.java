package com.feadev.eaids.nationalranking.model;

import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.nationalranking.model.enums.RankingScope;
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
@Table(name = "national_rankings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NationalRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "athlete_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID athleteUserId;

    @Column(name = "config_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID configId;

    @Column(name = "composite_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal compositeScore;

    @Column(name = "rank_value", nullable = false)
    private int rankValue;

    @Column(name = "total_athletes", nullable = false)
    private int totalAthletes;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentile;

    @Enumerated(EnumType.STRING)
    @Column(name = "ranking_scope", nullable = false)
    private RankingScope rankingScope;

    @Column(name = "scope_value", length = 100)
    private String scopeValue;

    @Enumerated(EnumType.STRING)
    @Column
    private FootballPosition position;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_category")
    private AgeCategory ageCategory;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;
}
