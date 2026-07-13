package com.feadev.eaids.registrations.dto.request;

import com.feadev.eaids.registrations.validation.PasswordMatch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for the scout password setup flow.
 * Used when a scout clicks the password setup link from their invitation email.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PasswordMatch
@Schema(description = "Request to set up a scout account password using the token from the invitation email")
public class SetupPasswordRequest {

    @NotBlank(message = "Token is required")
    @Schema(description = "Password setup token from the invitation email (72-hour expiry)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String token;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,128}$",
            message = "Password must contain at least 8 characters, one uppercase letter, one lowercase letter, one digit, and one special character, and must not exceed 128 characters"
    )
    @Schema(description = "New password (8-128 chars, must include uppercase, lowercase, digit, and special character)", example = "Sc0ut@Secur3")
    private String password;

    @NotBlank(message = "Confirm password is required")
    @Schema(description = "Must match the password field", example = "Sc0ut@Secur3")
    private String confirmPassword;
}
