package com.feadev.eaids.registrations.model;

import com.feadev.eaids.registrations.model.enums.AthleteInterest;
import com.feadev.eaids.registrations.model.enums.Gender;
import com.feadev.eaids.registrations.model.enums.SportsBackground;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "athlete_profiles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AthleteProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false)
    private int age;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Gender gender;

    @Column(name = "state_of_residence", nullable = false, length = 100)
    private String stateOfResidence;

    @Column(name = "country_of_residence", nullable = false, length = 100)
    private String countryOfResidence;

    @Column(nullable = false, length = 100)
    private String nationality;

    @Column(name = "height_cm", precision = 5, scale = 1)
    private BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 1)
    private BigDecimal weightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "athlete_interest", nullable = false)
    private AthleteInterest athleteInterest;

    @Builder.Default
    @Column(name = "disability_status", nullable = false)
    private boolean disabilityStatus = false;

    @ElementCollection
    @CollectionTable(
            name = "athlete_sports_backgrounds",
            joinColumns = @JoinColumn(name = "athlete_profile_id")
    )
    @Column(name = "sports_background")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<SportsBackground> sportsBackgrounds = new HashSet<>();

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    @Column(name = "icon_image_url")
    private String iconImageUrl;

    @OneToMany(mappedBy = "athleteProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AthleteVideo> videos = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private LocalDateTime lastModifiedDate;
}
