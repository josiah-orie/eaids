package com.feadev.eaids.registrations.dto.request;

import com.feadev.eaids.registrations.validation.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuardianConsentRequest {

    @NotBlank(message = "Guardian name is required")
    @Size(min = 1, max = 100, message = "Guardian name must be between 1 and 100 characters")
    private String guardianName;

    @NotBlank(message = "Guardian email is required")
    @Email(message = "Guardian email must be a valid email address")
    private String guardianEmail;

    @NotBlank(message = "Guardian phone is required")
    @PhoneNumber
    private String guardianPhone;

    @NotNull(message = "Consent acceptance is required")
    private Boolean consentAccepted;

    @NotBlank(message = "Electronic signature is required")
    @Size(min = 1, max = 500, message = "Electronic signature must be between 1 and 500 characters")
    private String electronicSignature;
}
