package com.feadev.eaids.medical.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInjuryRequest {

    // Public fields — any authorised reporter may update
    private LocalDateTime incidentDate;
    private String incidentLocation;
    private String injuryContext;
    private String bodyRegion;
    private String injuryCategory;
    private String mechanismOfInjury;
    private String symptomsReported;
    private String initialResponse;

    // Clinician-only fields — service enforces role check before applying
    private String severity;
    private String confirmedDiagnosis;
    private String referralDetails;
    private String treatmentPlan;
    private String participationRestrictions;
    private String rehabilitationStatus;
    private String returnToSportDecision;
}
