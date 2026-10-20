package com.feadev.eaids.medical.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateAssessmentRequest {

    @NotBlank(message = "Assessment type is required")
    private String assessmentType;

    @NotNull(message = "Assessment date is required")
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
