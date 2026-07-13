package com.feadev.eaids.trainingload.dto.response;

import com.feadev.eaids.trainingload.model.enums.TrainingIntensity;
import com.feadev.eaids.trainingload.model.enums.TrainingSessionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingLoadResponse {

    private UUID id;
    private UUID enrollmentId;
    private UUID campId;
    private LocalDate sessionDate;
    private int durationMinutes;
    private TrainingIntensity intensity;
    private TrainingSessionType sessionType;
    private UUID recordedBy;
    private String notes;
    private LocalDateTime createdDate;
}
