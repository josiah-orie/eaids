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
public class AthleteScoreResponse {

    private UUID id;
    private UUID enrollmentId;
    private String athleteName;
    private UUID campId;
    private FootballPosition position;
    private BigDecimal physicalScore;
    private BigDecimal technicalScore;
    private BigDecimal tacticalScore;
    private BigDecimal attendanceScore;
    private BigDecimal disciplineScore;
    private BigDecimal coachReviewScore;
    private BigDecimal matchStatsScore;
    private BigDecimal totalWeightedScore;
    private LocalDateTime calculatedAt;
}
