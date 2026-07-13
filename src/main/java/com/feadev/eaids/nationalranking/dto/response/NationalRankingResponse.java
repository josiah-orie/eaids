package com.feadev.eaids.nationalranking.dto.response;

import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.nationalranking.model.enums.RankingScope;
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
public class NationalRankingResponse {

    private UUID id;
    private UUID athleteUserId;
    private String athleteName;
    private BigDecimal compositeScore;
    private int rankValue;
    private int totalAthletes;
    private BigDecimal percentile;
    private RankingScope rankingScope;
    private String scopeValue;
    private FootballPosition position;
    private AgeCategory ageCategory;
    private LocalDateTime generatedAt;
}
