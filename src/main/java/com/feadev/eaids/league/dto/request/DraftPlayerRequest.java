package com.feadev.eaids.league.dto.request;

import com.feadev.eaids.technical.model.enums.FootballPosition;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DraftPlayerRequest {

    @NotNull(message = "Athlete user ID is required")
    private UUID athleteUserId;

    @Min(value = 1, message = "Jersey number must be at least 1")
    private Integer jerseyNumber;

    @NotNull(message = "Position is required")
    private FootballPosition position;
}
