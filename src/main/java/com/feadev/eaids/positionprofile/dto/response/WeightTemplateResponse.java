package com.feadev.eaids.positionprofile.dto.response;

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
public class WeightTemplateResponse {

    private UUID id;
    private BigDecimal physicalWeight;
    private BigDecimal technicalWeight;
    private BigDecimal tacticalWeight;
    private BigDecimal attendanceWeight;
    private BigDecimal disciplineWeight;
    private BigDecimal coachReviewWeight;
    private BigDecimal matchStatsWeight;
}
