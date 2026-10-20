package com.feadev.eaids.medical.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "athlete_medical_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AthleteMedicalProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "athlete_user_id", nullable = false, unique = true,
            columnDefinition = "BINARY(16)")
    private UUID athleteUserId;

    @Column(name = "blood_type", length = 10)
    private String bloodType;

    @Column(name = "existing_conditions", columnDefinition = "TEXT")
    private String existingConditions;

    @Column(name = "past_illnesses", columnDefinition = "TEXT")
    private String pastIllnesses;

    @Column(name = "previous_surgeries", columnDefinition = "TEXT")
    private String previousSurgeries;

    @Column(name = "hospital_admissions", columnDefinition = "TEXT")
    private String hospitalAdmissions;

    @Column(name = "known_allergies", columnDefinition = "TEXT")
    private String knownAllergies;

    @Column(name = "current_medications", columnDefinition = "TEXT")
    private String currentMedications;

    @Builder.Default
    @Column(name = "family_cardiac_history", nullable = false)
    private boolean familyCardiacHistory = false;

    @Column(name = "exercise_symptoms", columnDefinition = "TEXT")
    private String exerciseSymptoms;

    @Column(name = "vision_hearing_notes", columnDefinition = "TEXT")
    private String visionHearingNotes;

    @Column(name = "musculoskeletal_notes", columnDefinition = "TEXT")
    private String musculoskeletalNotes;

    // Emergency contact
    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 30)
    private String emergencyContactPhone;

    @Column(name = "emergency_contact_relation", length = 50)
    private String emergencyContactRelation;

    // Disability / wheelchair-specific (all optional)
    @Column(name = "neurological_spinal_history", columnDefinition = "TEXT")
    private String neurologicalSpinalHistory;

    @Column(name = "functional_limitations", columnDefinition = "TEXT")
    private String functionalLimitations;

    @Column(name = "assistive_equipment", columnDefinition = "TEXT")
    private String assistiveEquipment;

    @Column(name = "individual_emergency_care_notes", columnDefinition = "TEXT")
    private String individualEmergencyCareNotes;

    // Declaration metadata
    @Builder.Default
    @Column(name = "questionnaire_completed", nullable = false)
    private boolean questionnaireCompleted = false;

    @Builder.Default
    @Column(name = "reviewed_by_medical_officer", nullable = false)
    private boolean reviewedByMedicalOfficer = false;

    @Column(name = "submitted_by", columnDefinition = "BINARY(16)")
    private UUID submittedBy;

    @Column(name = "submitted_date")
    private LocalDateTime submittedDate;

    @Column(name = "last_reviewed_date")
    private LocalDateTime lastReviewedDate;

    @Column(name = "outstanding_clarifications", columnDefinition = "TEXT")
    private String outstandingClarifications;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @LastModifiedBy
    @Column(name = "last_modified_by")
    private String lastModifiedBy;
}
