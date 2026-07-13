package com.feadev.eaids.registrations.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoachProfileResponse {

    private UUID id;
    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private String nationality;
    private String stateOfResidence;
    private String phoneNumber;
    private String emailAddress;
    private String currentSport;
    private String currentOrganization;
    private String currentPosition;
    private String yearsOfExperience;
    private String areaOfInterest;
    private String availability;
    private String americanFootballExperience;
    private String involvementLevel;
    private Set<String> coachingInterests;
    private List<DocumentResponse> documents;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentResponse {
        private UUID id;
        private String documentType;
        private String fileName;
        private Long fileSize;
        private String verificationStatus;
        private LocalDateTime uploadedAt;
    }
}
