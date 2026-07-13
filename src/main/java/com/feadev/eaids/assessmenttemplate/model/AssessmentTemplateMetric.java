package com.feadev.eaids.assessmenttemplate.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "assessment_template_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentTemplateMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private AssessmentTemplate template;

    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;

    @Column(length = 50)
    private String category;

    @Column(length = 30)
    private String unit;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "max_score", nullable = false)
    private int maxScore;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;
}
