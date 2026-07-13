package com.feadev.eaids.scouting.dto.response;

import com.feadev.eaids.technical.model.enums.FootballPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AthleteReportResponse {

    private UUID athleteUserId;
    private String fullName;
    private int age;
    private String country;
    private String state;
    private FootballPosition position;

    // Scores summary
    private BigDecimal latestPhysicalScore;
    private BigDecimal latestTechnicalScore;
    private BigDecimal latestTacticalScore;
    private BigDecimal latestTotalWeightedScore;

    // Ranking
    private Integer nationalRank;
    private BigDecimal compositeScore;

    // Recommendation
    private FootballPosition recommendedPosition;
    private BigDecimal positionFitScore;

    // Potential
    private String potentialTier;
    private BigDecimal projectedScore1yr;

    // Videos
    private List<String> videoUrls;

    private LocalDateTime generatedAt;
}
