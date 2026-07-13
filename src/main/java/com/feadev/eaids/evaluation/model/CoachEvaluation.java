package com.feadev.eaids.evaluation.model;

import com.feadev.eaids.enrollment.model.CampEnrollment;
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
@Table(name = "coach_evaluations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_evaluation_enrollment_evaluator",
                columnNames = {"enrollment_id", "evaluator_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CoachEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private CampEnrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evaluator_id", nullable = false)
    private CampEvaluator evaluator;

    @Column(name = "technical_score", precision = 5, scale = 2)
    private BigDecimal technicalScore;

    @Column(name = "tactical_score", precision = 5, scale = 2)
    private BigDecimal tacticalScore;

    @Column(name = "leadership_score", precision = 5, scale = 2)
    private BigDecimal leadershipScore;

    @Column(name = "character_score", precision = 5, scale = 2)
    private BigDecimal characterScore;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(columnDefinition = "TEXT")
    private String comments;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;
}
