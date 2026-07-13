package com.feadev.eaids.camp.dto.request;

import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.camp.model.enums.AggregationStrategy;
import com.feadev.eaids.camp.model.enums.CampType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCampRequest {

    @Size(max = 200, message = "Camp name must not exceed 200 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private CampType campType;

    private AgeCategory ageCategory;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    private LocalDate startDate;

    private LocalDate endDate;

    private LocalDate registrationDeadline;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 10000, message = "Capacity must not exceed 10000")
    private Integer capacity;

    private AggregationStrategy aggregationStrategy;
}
