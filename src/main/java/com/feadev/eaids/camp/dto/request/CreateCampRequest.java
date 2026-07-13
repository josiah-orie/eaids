package com.feadev.eaids.camp.dto.request;

import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.camp.model.enums.AggregationStrategy;
import com.feadev.eaids.camp.model.enums.CampType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCampRequest {

    @NotBlank(message = "Camp name is required")
    @Size(max = 200, message = "Camp name must not exceed 200 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Camp type is required")
    private CampType campType;

    @NotNull(message = "Age category is required")
    private AgeCategory ageCategory;

    @NotBlank(message = "Location is required")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be in the present or future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    private LocalDate registrationDeadline;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10000, message = "Capacity must not exceed 10000")
    private Integer capacity;

    private AggregationStrategy aggregationStrategy;
}
