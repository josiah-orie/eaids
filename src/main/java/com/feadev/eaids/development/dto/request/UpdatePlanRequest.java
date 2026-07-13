package com.feadev.eaids.development.dto.request;

import com.feadev.eaids.development.model.enums.DevelopmentPlanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePlanRequest {

    private String overallAssessment;
    private UUID coachUserId;
    private DevelopmentPlanStatus status;
    private LocalDate startDate;
    private LocalDate targetEndDate;
}
