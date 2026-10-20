package com.feadev.eaids.medical.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "athlete_injuries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AthleteInjury {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "athlete_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID athleteUserId;

    /** Optional link to a camp during which the injury occurred. */
    @Column(name = "camp_id", columnDefinition = "BINARY(16)")
    private UUID campId;

    @Column(name = "incident_date", nullable = false)
    private LocalDateTime incidentDate;

    @Column(name = "reported_date", nullable = false)
    private LocalDateTime reportedDate;

    @Column(name = "incident_location", length = 200)
    private String incidentLocation;

    @Column(name = "injury_context", length = 50)
    private String injuryContext;

    @Column(name = "body_region", length = 50)
    private String bodyRegion;

    @Column(name = "injury_category", length = 50)
    private String injuryCategory;

    @Builder.Default
    @Column(name = "is_suspected_concussion", nullable = false)
    private boolean isSuspectedConcussion = false;

    @Column(name = "mechanism_of_injury", columnDefinition = "TEXT")
    private String mechanismOfInjury;

    @Column(name = "symptoms_reported", columnDefinition = "TEXT")
    private String symptomsReported;

    /** Clinician-only field — excluded from athlete-facing response DTOs. */
    @Column(name = "severity", length = 30)
    private String severity;

    /** Clinician-only field — excluded from athlete-facing response DTOs. */
    @Column(name = "confirmed_diagnosis", columnDefinition = "TEXT")
    private String confirmedDiagnosis;

    @Column(name = "initial_response", columnDefinition = "TEXT")
    private String initialResponse;

    @Column(name = "referral_details", columnDefinition = "TEXT")
    private String referralDetails;

    @Column(name = "treatment_plan", columnDefinition = "TEXT")
    private String treatmentPlan;

    @Column(name = "participation_restrictions", columnDefinition = "TEXT")
    private String participationRestrictions;

    @Column(name = "rehabilitation_status", length = 50)
    private String rehabilitationStatus;

    @Column(name = "return_to_sport_decision", columnDefinition = "TEXT")
    private String returnToSportDecision;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 50)
    private String status = "REPORTED";

    /** The user who reported the injury — set from the authenticated principal. */
    @Column(name = "reported_by", nullable = false, columnDefinition = "BINARY(16)")
    private UUID reportedBy;

    @Column(name = "recorded_by", columnDefinition = "BINARY(16)")
    private UUID recordedBy;

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
