package com.feadev.eaids.development.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingRecommendationResponse {

    private UUID id;
    private String position;
    private String category;
    private String title;
    private String description;
    private String difficultyLevel;
    private boolean active;
}
