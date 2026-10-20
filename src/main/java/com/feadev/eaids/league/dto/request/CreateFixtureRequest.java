package com.feadev.eaids.league.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFixtureRequest {

    @NotNull(message = "Home team ID is required")
    private UUID homeTeamId;

    @NotNull(message = "Away team ID is required")
    private UUID awayTeamId;

    private UUID divisionId;

    @NotNull(message = "Scheduled date is required")
    private LocalDateTime scheduledDate;

    private String venue;

    private UUID refereeUserId;

    private String notes;
}
