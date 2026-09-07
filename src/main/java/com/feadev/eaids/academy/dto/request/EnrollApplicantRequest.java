package com.feadev.eaids.academy.dto.request;

import com.feadev.eaids.academy.model.enums.ApplicantType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Admin payload to enroll an approved applicant into a training schedule.
 * {@code applicationId} references an individual or institution application id.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollApplicantRequest {

    @NotNull(message = "Applicant type is required")
    private ApplicantType applicantType;

    @NotNull(message = "Application id is required")
    private UUID applicationId;
}
