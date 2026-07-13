package com.feadev.eaids.nationalranking.model;

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
@Table(name = "national_ranking_configs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NationalRankingConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "weight_formula", nullable = false, columnDefinition = "JSON")
    private String weightFormula;

    @Builder.Default
    @Column(name = "min_camps_required", nullable = false)
    private int minCampsRequired = 2;

    @Column(name = "age_categories", columnDefinition = "JSON")
    private String ageCategories;

    @Column(columnDefinition = "JSON")
    private String positions;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_by", columnDefinition = "BINARY(16)")
    private UUID createdBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;
}
