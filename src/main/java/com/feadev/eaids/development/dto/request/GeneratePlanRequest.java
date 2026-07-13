package com.feadev.eaids.development.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratePlanRequest {

    @NotNull(message = "Athlete user ID is required")
    private UUID athleteUserId;

    @NotNull(message = "Camp ID is required")
    private UUID campId;
}
