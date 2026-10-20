package com.feadev.eaids.league.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchFixtureResponse {
    private UUID id;
    private UUID leagueId;
    private String leagueName;
    private UUID divisionId;
    private String divisionName;
    private UUID homeTeamId;
    private String homeTeamName;
    private UUID awayTeamId;
    private String awayTeamName;
    private LocalDateTime scheduledDate;
    private String venue;
    private String status;
    private Integer homeScore;
    private Integer awayScore;
    private UUID refereeUserId;
    private String notes;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
