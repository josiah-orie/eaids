package com.feadev.eaids.camp.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampResponse {

    private UUID id;
    private String name;
    private String description;
    private String campType;
    private String status;
    private String ageCategory;
    private String location;
    private String country;
    private String state;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate registrationDeadline;
    private int capacity;
    private int currentEnrollment;
    private String aggregationStrategy;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
