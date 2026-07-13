package com.feadev.eaids.scouting.dto.request;

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
public class CreateBookmarkRequest {

    @NotNull(message = "Athlete user ID is required")
    private UUID athleteUserId;

    private String notes;
}
