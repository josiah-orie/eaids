package com.feadev.eaids.scoring.dto.response;

import com.feadev.eaids.technical.model.enums.FootballPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeightProfileResponse {

    private UUID id;
    private UUID campId;
    private FootballPosition position;
    private BigDecimal physicalWeight;
    private BigDecimal technicalWeight;
    private BigDecimal tacticalWeight;
    private BigDecimal attendanceWeight;
    private BigDecimal disciplineWeight;
    private BigDecimal coachReviewWeight;
    private BigDecimal matchStatsWeight;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
