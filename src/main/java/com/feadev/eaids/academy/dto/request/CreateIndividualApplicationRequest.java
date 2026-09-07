package com.feadev.eaids.academy.dto.request;

import com.feadev.eaids.academy.model.enums.Location;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Public onboarding payload for an individual academy applicant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIndividualApplicationRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150)
    private String fullName;

    @NotNull(message = "Age is required")
    @Min(value = 5, message = "Age must be at least 5")
    @Max(value = 60, message = "Age must be at most 60")
    private Integer age;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 255)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20)
    private String phone;

    @NotNull(message = "Location is required")
    private Location location;

    @NotNull(message = "Address is required")
    @Valid
    private AddressDto address;
}
