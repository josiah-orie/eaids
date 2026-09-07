package com.feadev.eaids.academy.dto.request;

import com.feadev.eaids.academy.model.enums.ScheduleTargetType;
import com.feadev.eaids.camp.model.enums.AgeCategory;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Admin payload for creating an academy training schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTrainingScheduleRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 5000)
    private String description;

    @NotNull(message = "Age category is required")
    private AgeCategory ageCategory;

    @NotNull(message = "Target type is required")
    private ScheduleTargetType targetType;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @FutureOrPresent(message = "End date must be today or in the future")
    private LocalDate endDate;

    @Size(max = 200)
    private String location;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
}
