package com.feadev.eaids.ranking.dto.response;

import com.feadev.eaids.ranking.model.enums.RankingType;
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
public class RankingResponse {

    private UUID id;
    private UUID enrollmentId;
    private String athleteName;
    private UUID campId;
    private RankingType rankingType;
    private int rankValue;
    private int totalInCategory;
    private BigDecimal percentile;
    private BigDecimal score;
    private String filterValue;
    private LocalDateTime calculatedAt;
}
