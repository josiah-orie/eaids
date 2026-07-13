package com.feadev.eaids.registrations.model;

import com.feadev.eaids.registrations.model.enums.AmericanFootballExperience;
import com.feadev.eaids.registrations.model.enums.AreaOfInterest;
import com.feadev.eaids.registrations.model.enums.Availability;
import com.feadev.eaids.registrations.model.enums.CoachingInterest;
import com.feadev.eaids.registrations.model.enums.Gender;
import com.feadev.eaids.registrations.model.enums.InvolvementLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "coach_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class CoachProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, length = 100)
    private String nationality;

    @Column(name = "state_of_residence", nullable = false, length = 100)
    private String stateOfResidence;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "email_address", length = 255)
    private String emailAddress;

    @Column(name = "current_sport", length = 100)
    private String currentSport;

    @Column(name = "current_organization", length = 100)
    private String currentOrganization;

    @Column(name = "current_position", length = 100)
    private String currentPosition;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @Enumerated(EnumType.STRING)
    @Column(name = "area_of_interest", nullable = false)
    private AreaOfInterest areaOfInterest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Availability availability;

    @Enumerated(EnumType.STRING)
    @Column(name = "american_football_experience", nullable = false)
    private AmericanFootballExperience americanFootballExperience;

    @Enumerated(EnumType.STRING)
    @Column(name = "involvement_level", nullable = false)
    private InvolvementLevel involvementLevel;

    @ElementCollection
    @CollectionTable(
            name = "coach_coaching_interests",
            joinColumns = @JoinColumn(name = "coach_profile_id")
    )
    @Column(name = "coaching_interest")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<CoachingInterest> coachingInterests = new HashSet<>();

    @OneToMany(mappedBy = "coachProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CoachDocument> documents = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;
}
