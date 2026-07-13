package com.feadev.eaids.analytics.recommendation.dto;

import com.feadev.eaids.technical.model.enums.FootballPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PositionFitScore {

    private FootballPosition position;
    private BigDecimal fitScore;
    private String profileName;
}
