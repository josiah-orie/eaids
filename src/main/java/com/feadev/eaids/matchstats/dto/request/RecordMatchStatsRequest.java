package com.feadev.eaids.matchstats.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordMatchStatsRequest {

    @NotNull(message = "Enrollment ID is required")
    private UUID enrollmentId;

    @NotNull(message = "Match date is required")
    private LocalDate matchDate;

    private String opponent;

    @NotEmpty(message = "At least one stat entry is required")
    @Valid
    private List<StatEntryRequest> stats;
}
