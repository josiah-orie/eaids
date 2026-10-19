package com.feadev.eaids.league.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamPlayerResponse {
    private UUID teamPlayerId;
    private UUID athleteId;
    private String athleteEmail;
    private Integer jerseyNumber;
    private String position;
    private String status;
    private LocalDate draftedDate;
    private LocalDate releasedDate;
}
