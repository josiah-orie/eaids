package com.feadev.eaids.medical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Coach- and scout-facing eligibility summary.
 * Contains ONLY operational restrictions — no diagnoses, no clinical notes,
 * no details about the underlying medical condition.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationEligibilityResponse {

    private UUID athleteUserId;
    private String activityScope;

    /**
     * {@code true} if the most recent clearance is CLEARED or CLEARED_WITH_RESTRICTIONS
     * and has not expired or been revoked.
     */
    private boolean eligible;

    /** Participation restrictions from the clearance — operational info only. */
    private String restrictions;

    /** Required accommodations from the clearance. */
    private String requiredAccommodations;

    private LocalDate expiryDate;

    /** The clearance status, or {@code null} if no record is on file. */
    private String status;

    /**
     * Human-readable note, e.g. "No medical clearance on file" or
     * "Clearance has expired — reassessment required."
     */
    private String note;
}
