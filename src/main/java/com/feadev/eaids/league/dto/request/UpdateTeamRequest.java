package com.feadev.eaids.league.dto.request;

import com.feadev.eaids.league.model.enums.TeamStatus;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTeamRequest {

    @Size(max = 200, message = "Team name must not exceed 200 characters")
    private String name;

    private UUID divisionId;

    @Size(max = 200, message = "Home venue must not exceed 200 characters")
    private String homeVenue;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    private TeamStatus status;
}
