package com.feadev.eaids.camp.model;

import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.camp.model.enums.AggregationStrategy;
import com.feadev.eaids.camp.model.enums.CampStatus;
import com.feadev.eaids.camp.model.enums.CampType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "camps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Camp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "camp_type", nullable = false)
    private CampType campType;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampStatus status = CampStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_category", nullable = false)
    private AgeCategory ageCategory;

    @Column(nullable = false, length = 200)
    private String location;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(length = 100)
    private String state;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "registration_deadline")
    private LocalDate registrationDeadline;

    @Column(nullable = false)
    private int capacity;

    @Builder.Default
    @Column(name = "current_enrollment", nullable = false)
    private int currentEnrollment = 0;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "aggregation_strategy", nullable = false)
    private AggregationStrategy aggregationStrategy = AggregationStrategy.AVERAGE;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;
}
