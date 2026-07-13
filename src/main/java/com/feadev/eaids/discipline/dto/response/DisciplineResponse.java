package com.feadev.eaids.discipline.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisciplineResponse {

    private UUID id;
    private UUID enrollmentId;
    private UUID campId;
    private String infractionType;
    private int penaltyPoints;
    private String description;
    private LocalDate infractionDate;
    private String recordedBy;
    private LocalDateTime createdDate;
}
