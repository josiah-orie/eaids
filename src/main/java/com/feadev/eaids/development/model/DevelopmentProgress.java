package com.feadev.eaids.development.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "development_progress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevelopmentProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private DevelopmentPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id")
    private DevelopmentGoal goal;

    @Column(precision = 10, scale = 2)
    private BigDecimal value;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recorded_by", columnDefinition = "BINARY(16)")
    private UUID recordedBy;

    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;
}
