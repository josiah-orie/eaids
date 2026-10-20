package com.feadev.eaids.league.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamResponse {
    private UUID id;
    private String name;
    private UUID leagueId;
    private String leagueName;
    private UUID divisionId;
    private String divisionName;
    private String homeVenue;
    private String country;
    private String state;
    private String logoUrl;
    private String status;
    // Current coaching staff — null if not yet assigned
    private TeamCoachResponse headCoach;
    private TeamCoachResponse assistantCoach;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
