package com.feadev.eaids.scouting.dto.request;

import com.feadev.eaids.scouting.model.enums.InterviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewInterviewRequest {

    @NotNull(message = "Status is required (APPROVED or REJECTED)")
    private InterviewStatus status;

    private String reviewNotes;
}
