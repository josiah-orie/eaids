package com.feadev.eaids.medical.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medical_assessments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MedicalAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "athlete_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID athleteUserId;

    @Column(name = "assessment_type", nullable = false, length = 50)
    private String assessmentType;

    @Column(name = "assessment_date", nullable = false)
    private LocalDateTime assessmentDate;

    /** Set from the authenticated principal — never from the client request body. */
    @Column(name = "clinician_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID clinicianUserId;

    @Column(name = "clinician_credentials", length = 200)
    private String clinicianCredentials;

    @Column(name = "blood_pressure_systolic")
    private Integer bloodPressureSystolic;

    @Column(name = "blood_pressure_diastolic")
    private Integer bloodPressureDiastolic;

    @Column(name = "pulse_bpm")
    private Integer pulseBpm;

    @Column(name = "cardiovascular_findings", columnDefinition = "TEXT")
    private String cardiovascularFindings;

    @Column(name = "respiratory_findings", columnDefinition = "TEXT")
    private String respiratoryFindings;

    @Column(name = "neurological_findings", columnDefinition = "TEXT")
    private String neurologicalFindings;

    @Column(name = "musculoskeletal_findings", columnDefinition = "TEXT")
    private String musculoskeletalFindings;

    @Column(name = "investigations_results", columnDefinition = "TEXT")
    private String investigationsResults;

    @Column(name = "clinical_findings", columnDefinition = "TEXT")
    private String clinicalFindings;

    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "participation_restrictions", columnDefinition = "TEXT")
    private String participationRestrictions;

    @Column(name = "referrals_required", columnDefinition = "TEXT")
    private String referralsRequired;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 30)
    private String status = "DRAFT";

    @Column(name = "finalised_at")
    private LocalDateTime finalisedAt;

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
