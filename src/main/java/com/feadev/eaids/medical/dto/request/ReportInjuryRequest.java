package com.feadev.eaids.medical.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportInjuryRequest {

    @NotNull(message = "Incident date is required")
    private LocalDateTime incidentDate;

    @NotNull(message = "Reported date is required")
    private LocalDateTime reportedDate;

    private UUID campId;
    private String incidentLocation;
    private String injuryContext;
    private String bodyRegion;
    private String injuryCategory;

    @Builder.Default
    private boolean isSuspectedConcussion = false;

    private String mechanismOfInjury;
    private String symptomsReported;
    private String initialResponse;
}
