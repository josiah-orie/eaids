package com.feadev.eaids.development.dto.request;

import com.feadev.eaids.technical.model.enums.FootballPosition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrainingRecommendationRequest {

    @NotNull(message = "Position is required")
    private FootballPosition position;

    @NotBlank(message = "Category is required")
    @Size(max = 50)
    private String category;

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    private String description;

    @NotBlank(message = "Difficulty level is required")
    @Size(max = 20)
    private String difficultyLevel;
}
