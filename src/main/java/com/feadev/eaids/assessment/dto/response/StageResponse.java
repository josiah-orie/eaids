package com.feadev.eaids.assessment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StageResponse {

    private UUID id;
    private UUID campId;
    private String stageName;
    private int stageOrder;
    private String description;
    private Integer durationDays;
    private BigDecimal scoreWeight;
    private boolean isActive;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
