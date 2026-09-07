package com.feadev.eaids.academy.dto.request;

import com.feadev.eaids.academy.model.enums.InstitutionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Public onboarding payload for an institution academy applicant.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInstitutionApplicationRequest {

    @NotNull(message = "Institution type is required")
    private InstitutionType institutionType;

    @NotBlank(message = "Institution name is required")
    @Size(max = 200)
    private String name;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Contact email must be valid")
    @Size(max = 255)
    private String contactEmail;

    @NotBlank(message = "Contact phone is required")
    @Size(max = 20)
    private String contactPhone;

    @NotNull(message = "Address is required")
    @Valid
    private AddressDto address;
}
