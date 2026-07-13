package com.feadev.eaids.nationalranking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingConfigResponse {

    private UUID id;
    private String name;
    private String description;
    private String weightFormula;
    private int minCampsRequired;
    private String ageCategories;
    private String positions;
    private boolean active;
    private LocalDateTime createdDate;
}
