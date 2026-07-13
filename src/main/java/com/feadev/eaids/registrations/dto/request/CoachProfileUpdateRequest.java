package com.feadev.eaids.registrations.dto.request;

import com.feadev.eaids.registrations.model.enums.AmericanFootballExperience;
import com.feadev.eaids.registrations.model.enums.AreaOfInterest;
import com.feadev.eaids.registrations.model.enums.Availability;
import com.feadev.eaids.registrations.model.enums.CoachingInterest;
import com.feadev.eaids.registrations.model.enums.InvolvementLevel;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachProfileUpdateRequest {

    private String fullName;

    private String nationality;

    private String stateOfResidence;

    private String phoneNumber;

    private String emailAddress;

    private String currentSport;

    private String currentOrganization;

    private String currentPosition;

    private Integer yearsOfExperience;

    private AreaOfInterest areaOfInterest;

    private Availability availability;

    private AmericanFootballExperience americanFootballExperience;

    private InvolvementLevel involvementLevel;

    @Size(min = 1, message = "At least one coaching interest is required")
    private Set<CoachingInterest> coachingInterests;
}
