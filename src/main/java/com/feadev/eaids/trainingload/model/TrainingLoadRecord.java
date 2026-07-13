package com.feadev.eaids.trainingload.model;

import com.feadev.eaids.trainingload.model.enums.TrainingIntensity;
import com.feadev.eaids.trainingload.model.enums.TrainingSessionType;
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
@Table(name = "training_load_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TrainingLoadRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "enrollment_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID enrollmentId;

    @Column(name = "camp_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID campId;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingIntensity intensity;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_type", nullable = false)
    private TrainingSessionType sessionType;

    @Column(name = "recorded_by", nullable = false, columnDefinition = "BINARY(16)")
    private UUID recordedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
}
