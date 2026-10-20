package com.feadev.eaids.medical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Full clearance response for medical officers, admins, and athletes (own record).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalClearanceResponse {

    private UUID id;
    private UUID athleteUserId;
    private UUID issuedBy;
    private UUID relatedAssessmentId;
    private UUID relatedInjuryId;
    private String activityScope;
    private String status;
    private LocalDateTime decisionDate;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private String participationRestrictions;
    private String requiredAccommodations;
    private String followUpRequirements;
    private String conditionalReason;
    private String revocationReason;
    private UUID revokedBy;
    private LocalDateTime revokedAt;
    private UUID previousClearanceId;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
