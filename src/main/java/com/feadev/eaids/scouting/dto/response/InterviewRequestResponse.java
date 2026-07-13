package com.feadev.eaids.scouting.dto.response;

import com.feadev.eaids.scouting.model.enums.InterviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewRequestResponse {

    private UUID id;
    private UUID scoutUserId;
    private UUID athleteUserId;
    private List<String> preferredDates;
    private String notes;
    private InterviewStatus status;
    private UUID reviewedBy;
    private String reviewNotes;
    private LocalDateTime reviewDate;
    private LocalDateTime createdDate;
}
