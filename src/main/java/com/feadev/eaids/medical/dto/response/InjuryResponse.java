package com.feadev.eaids.medical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Full injury response for medical officers, physiotherapists, and admins.
 * Contains all clinical fields including severity and confirmed diagnosis.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InjuryResponse {

    private UUID id;
    private UUID athleteUserId;
    private UUID campId;
    private LocalDateTime incidentDate;
    private LocalDateTime reportedDate;
    private String incidentLocation;
    private String injuryContext;
    private String bodyRegion;
    private String injuryCategory;
    private boolean isSuspectedConcussion;
    private String mechanismOfInjury;
    private String symptomsReported;
    private String severity;
    private String confirmedDiagnosis;
    private String initialResponse;
    private String referralDetails;
    private String treatmentPlan;
    private String participationRestrictions;
    private String rehabilitationStatus;
    private String returnToSportDecision;
    private String status;
    private UUID reportedBy;
    private UUID recordedBy;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
