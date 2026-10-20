package com.feadev.eaids.league.dto.request;

import com.feadev.eaids.league.model.enums.TeamCoachRole;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignTeamCoachRequest {

    @NotNull(message = "Coach user ID is required")
    private UUID coachUserId;

    @NotNull(message = "Role is required")
    private TeamCoachRole role;

    private String notes;
}
