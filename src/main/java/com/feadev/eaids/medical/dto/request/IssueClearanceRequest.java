package com.feadev.eaids.medical.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IssueClearanceRequest {

    /** One of: CLEARED, CLEARED_WITH_RESTRICTIONS, NOT_CLEARED */
    @NotBlank(message = "Clearance status is required")
    private String status;

    private String activityScope;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;
    private String participationRestrictions;
    private String requiredAccommodations;
    private String followUpRequirements;

    /** Required when status is CLEARED_WITH_RESTRICTIONS or NOT_CLEARED */
    private String conditionalReason;

    /** Optional link to a previous assessment */
    private UUID relatedAssessmentId;

    /** Optional link to an injury this clearance addresses */
    private UUID relatedInjuryId;

    /** Optional reference to a previous clearance being superseded */
    private UUID previousClearanceId;
}
