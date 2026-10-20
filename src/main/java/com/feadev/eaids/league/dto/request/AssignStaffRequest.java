package com.feadev.eaids.league.dto.request;

import com.feadev.eaids.league.model.enums.LeagueStaffRole;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignStaffRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Staff role is required")
    private LeagueStaffRole staffRole;

    private String notes;
}
