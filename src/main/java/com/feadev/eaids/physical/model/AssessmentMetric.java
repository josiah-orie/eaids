package com.feadev.eaids.physical.model;

import com.feadev.eaids.camp.model.Camp;
import com.feadev.eaids.physical.model.enums.MeasurementUnit;
import com.feadev.eaids.physical.model.enums.MetricCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assessment_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AssessmentMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camp_id", nullable = false)
    private Camp camp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MetricCategory category;

    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeasurementUnit unit;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * If true, lower raw values get higher scores (e.g. time-based metrics like 40yd dash).
     */
    @Builder.Default
    @Column(name = "lower_is_better", nullable = false)
    private boolean lowerIsBetter = false;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;
}
