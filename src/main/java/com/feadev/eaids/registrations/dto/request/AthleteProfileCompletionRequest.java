package com.feadev.eaids.registrations.dto.request;

import com.feadev.eaids.registrations.model.enums.AthleteInterest;
import com.feadev.eaids.registrations.model.enums.Gender;
import com.feadev.eaids.registrations.model.enums.SportsBackground;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AthleteProfileCompletionRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "State of residence is required")
    @Size(max = 100, message = "State of residence must not exceed 100 characters")
    private String stateOfResidence;

    @NotBlank(message = "Country of residence is required")
    @Size(max = 100, message = "Country of residence must not exceed 100 characters")
    private String countryOfResidence;

    @NotBlank(message = "Nationality is required")
    @Size(max = 100, message = "Nationality must not exceed 100 characters")
    private String nationality;

    @DecimalMin(value = "50", message = "Height must be at least 50 cm")
    @DecimalMax(value = "300", message = "Height must not exceed 300 cm")
    private BigDecimal heightCm;

    @DecimalMin(value = "20", message = "Weight must be at least 20 kg")
    @DecimalMax(value = "300", message = "Weight must not exceed 300 kg")
    private BigDecimal weightKg;

    @NotEmpty(message = "At least one sports background is required")
    private Set<SportsBackground> sportsBackgrounds;

    @NotNull(message = "Athlete interest is required")
    private AthleteInterest athleteInterest;

    @Builder.Default
    private boolean disabilityStatus = false;

    @NotEmpty(message = "At least one video is required")
    @Size(min = 1, max = 10, message = "Videos must be between 1 and 10")
    @Valid
    private List<VideoAddRequest> videos;
}
