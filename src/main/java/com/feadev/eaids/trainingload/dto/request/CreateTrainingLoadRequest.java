package com.feadev.eaids.trainingload.dto.request;

import com.feadev.eaids.trainingload.model.enums.TrainingIntensity;
import com.feadev.eaids.trainingload.model.enums.TrainingSessionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrainingLoadRequest {

    @NotNull(message = "Session date is required")
    private LocalDate sessionDate;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be greater than 0")
    private Integer durationMinutes;

    @NotNull(message = "Intensity is required")
    private TrainingIntensity intensity;

    @NotNull(message = "Session type is required")
    private TrainingSessionType sessionType;

    private String notes;
}
