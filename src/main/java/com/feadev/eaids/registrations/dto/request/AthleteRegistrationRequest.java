package com.feadev.eaids.registrations.dto.request;

import com.feadev.eaids.registrations.validation.PasswordMatch;
import com.feadev.eaids.registrations.validation.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PasswordMatch
public class AthleteRegistrationRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Phone number is required")
    @PhoneNumber
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,128}$",
            message = "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one digit, and one special character, and must not exceed 128 characters"
    )
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;
}
