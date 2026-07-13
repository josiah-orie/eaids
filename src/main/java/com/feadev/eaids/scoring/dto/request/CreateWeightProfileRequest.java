package com.feadev.eaids.scoring.dto.request;

import com.feadev.eaids.technical.model.enums.FootballPosition;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWeightProfileRequest {

    @NotNull(message = "Position is required")
    private FootballPosition position;

    private BigDecimal physicalWeight;
    private BigDecimal technicalWeight;
    private BigDecimal tacticalWeight;
    private BigDecimal attendanceWeight;
    private BigDecimal disciplineWeight;
    private BigDecimal coachReviewWeight;
    private BigDecimal matchStatsWeight;

    // Validation that weights sum to 100 is enforced in the service layer
}
