package com.feadev.eaids.technical.model;

import com.feadev.eaids.camp.model.Camp;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "technical_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TechnicalMetric {

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

    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;

    @Column(name = "max_score", nullable = false)
    private int maxScore;

    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
}
