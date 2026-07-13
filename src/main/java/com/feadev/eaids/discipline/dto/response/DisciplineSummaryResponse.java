package com.feadev.eaids.discipline.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisciplineSummaryResponse {

    private UUID enrollmentId;
    private UUID campId;
    private int totalInfractions;
    private int totalPenaltyPoints;
    private BigDecimal disciplineScore;
}
