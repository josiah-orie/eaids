package com.feadev.eaids.league.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamCoachResponse {
    private UUID id;
    private UUID teamId;
    private String teamName;
    private UUID coachUserId;
    private String coachEmail;
    private String role;
    private String notes;
    private LocalDateTime assignedDate;
    private String assignedBy;
}
