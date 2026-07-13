package com.feadev.eaids.matchstats.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchStatDefinitionResponse {

    private UUID id;
    private String positionGroup;
    private String statName;
    private String unit;
    private int displayOrder;
    private boolean active;
}
