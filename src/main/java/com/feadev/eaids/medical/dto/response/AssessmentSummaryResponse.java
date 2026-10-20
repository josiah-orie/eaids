package com.feadev.eaids.medical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Athlete-facing assessment summary — excludes all clinical notes and findings.
 * Only exposes operational information the athlete is entitled to see.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssessmentSummaryResponse {

    private UUID id;
    private String assessmentType;
    private LocalDateTime assessmentDate;
    private String status;
    private LocalDate followUpDate;
    private String participationRestrictions;
    private LocalDateTime createdDate;
}
