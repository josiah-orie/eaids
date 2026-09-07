package com.feadev.eaids.academy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API representation of an academy training schedule.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingScheduleResponse {

    private UUID id;
    private String title;
    private String description;
    private String ageCategory;
    private String targetType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private int capacity;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
