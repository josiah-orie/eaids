package com.feadev.eaids.league.dto.request;

import com.feadev.eaids.league.model.enums.FixtureStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransitionFixtureRequest {

    @NotNull(message = "Status is required")
    private FixtureStatus status;

    private Integer homeScore;
    private Integer awayScore;
}
