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
@Table(name = "medical_clearances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MedicalClearance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "athlete_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID athleteUserId;

    /** Always set from the authenticated principal — never from the client request body. */
    @Column(name = "issued_by", nullable = false, columnDefinition = "BINARY(16)")
    private UUID issuedBy;

    @Column(name = "related_assessment_id", columnDefinition = "BINARY(16)")
    private UUID relatedAssessmentId;

    @Column(name = "related_injury_id", columnDefinition = "BINARY(16)")
    private UUID relatedInjuryId;

    @Column(name = "activity_scope", length = 200)
    private String activityScope;

    @Builder.Default
    @Column(name = "status", nullable = false, length = 50)
    private String status = "PENDING";

    @Column(name = "decision_date")
    private LocalDateTime decisionDate;

    @Column(name = "effective_date")
    private LocalDate effectiveDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "participation_restrictions", columnDefinition = "TEXT")
    private String participationRestrictions;

    @Column(name = "required_accommodations", columnDefinition = "TEXT")
    private String requiredAccommodations;

    @Column(name = "follow_up_requirements", columnDefinition = "TEXT")
    private String followUpRequirements;

    @Column(name = "conditional_reason", columnDefinition = "TEXT")
    private String conditionalReason;

    @Column(name = "revocation_reason", columnDefinition = "TEXT")
    private String revocationReason;

    @Column(name = "revoked_by", columnDefinition = "BINARY(16)")
    private UUID revokedBy;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "previous_clearance_id", columnDefinition = "BINARY(16)")
    private UUID previousClearanceId;

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
