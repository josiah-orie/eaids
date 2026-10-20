package com.feadev.eaids.league.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DivisionResponse {
    private UUID id;
    private UUID leagueId;
    private String leagueName;
    private String name;
    private int tierRank;
    private String description;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
