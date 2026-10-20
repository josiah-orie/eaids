package com.feadev.eaids.league.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequest {

    @NotBlank(message = "Team name is required")
    @Size(max = 200, message = "Team name must not exceed 200 characters")
    private String name;

    @NotNull(message = "League ID is required")
    private UUID leagueId;

    private UUID divisionId;

    @Size(max = 200, message = "Home venue must not exceed 200 characters")
    private String homeVenue;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;
}
