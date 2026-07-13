package com.feadev.eaids.development.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecordProgressRequest {

    private UUID goalId;

    @NotNull(message = "Recorded date is required")
    private LocalDate recordedDate;

    private BigDecimal value;
    private String notes;
}
