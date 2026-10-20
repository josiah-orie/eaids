package com.feadev.eaids.medical.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateMedicalProfileRequest {

    @Size(max = 10, message = "Blood type must not exceed 10 characters")
    private String bloodType;

    private String existingConditions;
    private String pastIllnesses;
    private String previousSurgeries;
    private String hospitalAdmissions;
    private String knownAllergies;
    private String currentMedications;
    private Boolean familyCardiacHistory;
    private String exerciseSymptoms;
    private String visionHearingNotes;
    private String musculoskeletalNotes;

    @Size(max = 100, message = "Emergency contact name must not exceed 100 characters")
    private String emergencyContactName;

    @Size(max = 30, message = "Emergency contact phone must not exceed 30 characters")
    private String emergencyContactPhone;

    @Size(max = 50, message = "Emergency contact relation must not exceed 50 characters")
    private String emergencyContactRelation;

    // Disability / wheelchair-specific (all optional)
    private String neurologicalSpinalHistory;
    private String functionalLimitations;
    private String assistiveEquipment;
    private String individualEmergencyCareNotes;

    // Declaration fields
    private Boolean questionnaireCompleted;
    private String outstandingClarifications;
}
