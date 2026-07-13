package com.feadev.eaids.scouting.dto.response;

import com.feadev.eaids.technical.model.enums.FootballPosition;
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
public class AthleteSearchResult {

    private UUID athleteUserId;
    private String fullName;
    private FootballPosition position;
    private int age;
    private String country;
    private String state;
    private BigDecimal compositeScore;
    private Integer nationalRank;
    private String potentialTier;
}
