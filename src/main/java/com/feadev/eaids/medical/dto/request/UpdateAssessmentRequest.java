package com.feadev.eaids.medical.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAssessmentRequest {

    private LocalDateTime assessmentDate;
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
}
