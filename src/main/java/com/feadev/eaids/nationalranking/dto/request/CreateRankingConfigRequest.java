package com.feadev.eaids.nationalranking.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRankingConfigRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Weight formula is required")
    private List<RecencyWeight> weightFormula;

    @Min(value = 1, message = "Minimum camps required must be at least 1")
    private int minCampsRequired;

    private List<String> ageCategories;
    private List<String> positions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecencyWeight {
        private int campRecencyIndex;
        private double weight;
    }
}
