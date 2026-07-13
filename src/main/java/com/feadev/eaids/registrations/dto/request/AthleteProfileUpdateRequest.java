package com.feadev.eaids.registrations.dto.request;

import com.feadev.eaids.registrations.model.enums.AthleteInterest;
import com.feadev.eaids.registrations.model.enums.SportsBackground;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AthleteProfileUpdateRequest {

    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @Size(max = 100, message = "State of residence must not exceed 100 characters")
    private String stateOfResidence;

    @Size(max = 100, message = "Country of residence must not exceed 100 characters")
    private String countryOfResidence;

    @Size(max = 100, message = "Nationality must not exceed 100 characters")
    private String nationality;

    @DecimalMin(value = "50", message = "Height must be at least 50 cm")
    @DecimalMax(value = "300", message = "Height must not exceed 300 cm")
    private BigDecimal heightCm;

    @DecimalMin(value = "20", message = "Weight must be at least 20 kg")
    @DecimalMax(value = "300", message = "Weight must not exceed 300 kg")
    private BigDecimal weightKg;

    private Set<SportsBackground> sportsBackgrounds;

    private AthleteInterest athleteInterest;

    private Boolean disabilityStatus;
}
