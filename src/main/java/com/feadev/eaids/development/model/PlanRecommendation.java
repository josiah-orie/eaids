package com.feadev.eaids.development.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "plan_recommendations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private DevelopmentPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    private TrainingRecommendation recommendation;

    @Column(nullable = false)
    private int priority;

    @Column(name = "coach_notes", columnDefinition = "TEXT")
    private String coachNotes;
}
