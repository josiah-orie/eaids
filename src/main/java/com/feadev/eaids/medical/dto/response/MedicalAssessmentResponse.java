package com.feadev.eaids.medical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Full assessment response for medical officers, physiotherapists, and admins.
 * Contains all clinical fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalAssessmentResponse {

    private UUID id;
    private UUID athleteUserId;
    private String assessmentType;
    private LocalDateTime assessmentDate;
    private UUID clinicianUserId;
    private String clinicianCredentials;
    private Integer bloodPressureSystolic;
    private Integer bloodPressureDiastolic;
    private Integer pulseBpm;
    private String cardiovascularFindings;
    private String respiratoryFindings;
    private String neurologicalFindings;
    private String musculoskeletalFindings;
    private String investigationsResults;
    private String clinicalFindings;
    private String recommendations;
    private String participationRestrictions;
    private String referralsRequired;
    private LocalDate followUpDate;
    private String status;
    private LocalDateTime finalisedAt;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
