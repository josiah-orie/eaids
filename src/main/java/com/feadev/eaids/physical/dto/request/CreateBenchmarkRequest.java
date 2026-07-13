package com.feadev.eaids.physical.dto.request;

import com.feadev.eaids.camp.model.enums.AgeCategory;
import com.feadev.eaids.registrations.model.enums.Gender;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBenchmarkRequest {

    @NotNull(message = "Min value is required")
    private BigDecimal minValue;

    @NotNull(message = "Max value is required")
    private BigDecimal maxValue;

    @NotNull(message = "Score is required")
    @Min(value = 0, message = "Score must be at least 0")
    @Max(value = 100, message = "Score must not exceed 100")
    private Integer score;

    private AgeCategory ageCategory;

    private Gender gender;
}
