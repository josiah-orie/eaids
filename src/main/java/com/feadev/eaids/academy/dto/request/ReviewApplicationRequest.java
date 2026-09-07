package com.feadev.eaids.academy.dto.request;

import com.feadev.eaids.academy.model.enums.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Admin decision payload for reviewing an academy application.
 * A non-blank {@code reviewNotes} value is required when the decision is REJECTED.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewApplicationRequest {

    @NotNull(message = "Decision is required")
    private ApplicationStatus decision;

    @Size(max = 2000)
    private String reviewNotes;
}
