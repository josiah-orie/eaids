package com.feadev.eaids.development.model;

import com.feadev.eaids.technical.model.enums.FootballPosition;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "training_recommendations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FootballPosition position;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "difficulty_level", nullable = false, length = 20)
    private String difficultyLevel;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
}
