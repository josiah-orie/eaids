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
    /** Primary key of the TeamPlayer join record. */
    private UUID id;
    /** Alias for {@code id} — used by the roster endpoint for clarity. */
    private UUID teamPlayerId;
    private UUID teamId;
    private String teamName;
    /** Athlete's user ID. */
    private UUID athleteUserId;
    /** Alias for {@code athleteUserId} — used by the roster endpoint for clarity. */
    private UUID athleteId;
    private String athleteEmail;
    private Integer jerseyNumber;
    private String position;
    private String status;
    private LocalDate draftedDate;
    private LocalDate releasedDate;
}
