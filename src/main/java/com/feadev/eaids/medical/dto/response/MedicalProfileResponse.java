package com.feadev.eaids.medical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalProfileResponse {

    private UUID id;
    private UUID athleteUserId;
    private String bloodType;
    private String existingConditions;
    private String pastIllnesses;
    private String previousSurgeries;
    private String hospitalAdmissions;
    private String knownAllergies;
    private String currentMedications;
    private boolean familyCardiacHistory;
    private String exerciseSymptoms;
    private String visionHearingNotes;
    private String musculoskeletalNotes;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String emergencyContactRelation;
    private String neurologicalSpinalHistory;
    private String functionalLimitations;
    private String assistiveEquipment;
    private String individualEmergencyCareNotes;
    private boolean questionnaireCompleted;
    private boolean reviewedByMedicalOfficer;
    private UUID submittedBy;
    private LocalDateTime submittedDate;
    private LocalDateTime lastReviewedDate;
    private String outstandingClarifications;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
