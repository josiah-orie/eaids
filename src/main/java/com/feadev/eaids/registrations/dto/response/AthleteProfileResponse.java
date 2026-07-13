package com.feadev.eaids.registrations.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AthleteProfileResponse {

    private UUID id;
    private String fullName;
    private int age;
    private LocalDate dateOfBirth;
    private String gender;
    private String stateOfResidence;
    private String countryOfResidence;
    private String nationality;
    private BigDecimal heightCm;
    private BigDecimal weightKg;
    private String athleteInterest;
    private boolean disabilityStatus;
    private Set<String> sportsBackgrounds;
    private String profileImageUrl;
    private String iconImageUrl;
    private List<VideoResponse> videos;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoResponse {
        private UUID id;
        private String url;
        private String videoType;
        private LocalDateTime createdDate;
    }
}
