package com.feadev.eaids.discipline.model;

import com.feadev.eaids.discipline.model.enums.InfractionType;
import com.feadev.eaids.enrollment.model.CampEnrollment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "discipline_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DisciplineRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private CampEnrollment enrollment;

    @Column(name = "camp_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID campId;

    @Enumerated(EnumType.STRING)
    @Column(name = "infraction_type", nullable = false)
    private InfractionType infractionType;

    @Column(name = "penalty_points", nullable = false)
    private int penaltyPoints;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "infraction_date", nullable = false)
    private LocalDate infractionDate;

    @Column(name = "recorded_by")
    private String recordedBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
}
