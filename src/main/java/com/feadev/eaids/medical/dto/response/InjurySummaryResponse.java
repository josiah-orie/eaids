package com.feadev.eaids.medical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Athlete-facing injury summary — excludes clinician-only fields
 * ({@code severity} and {@code confirmedDiagnosis} are omitted).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InjurySummaryResponse {

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
    private String initialResponse;
    private String participationRestrictions;
    private String rehabilitationStatus;
    private String status;
    private LocalDateTime createdDate;
}
