package com.feadev.eaids.registrations.dto.request;

import com.feadev.eaids.registrations.model.enums.AmericanFootballExperience;
import com.feadev.eaids.registrations.model.enums.AreaOfInterest;
import com.feadev.eaids.registrations.model.enums.Availability;
import com.feadev.eaids.registrations.model.enums.CoachingInterest;
import com.feadev.eaids.registrations.model.enums.Gender;
import com.feadev.eaids.registrations.model.enums.InvolvementLevel;
import com.feadev.eaids.registrations.validation.PasswordMatch;
import com.feadev.eaids.registrations.validation.PhoneNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@PasswordMatch
public class CoachRegistrationRequest {

    // Account fields
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

    // Profile fields
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    private String nationality;

    private String stateOfResidence;

    private String profilePhoneNumber;

    private String emailAddress;

    private String currentSport;

    private String currentOrganization;

    private String currentPosition;

    private Integer yearsOfExperience;

    @NotNull(message = "Area of interest is required")
    private AreaOfInterest areaOfInterest;

    @NotNull(message = "Availability is required")
    private Availability availability;

    @NotNull(message = "American football experience is required")
    private AmericanFootballExperience americanFootballExperience;

    @NotNull(message = "Involvement level is required")
    private InvolvementLevel involvementLevel;

    @NotEmpty(message = "At least one coaching interest is required")
    private Set<CoachingInterest> coachingInterests;
}
