package com.feadev.eaids.league.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueStaffResponse {
    private UUID id;
    private UUID leagueId;
    private String leagueName;
    private UUID userId;
    private String userEmail;
    private String staffRole;
    private String notes;
    private LocalDateTime assignedDate;
}
