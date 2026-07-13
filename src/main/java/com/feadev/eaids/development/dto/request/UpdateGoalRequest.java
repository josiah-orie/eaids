package com.feadev.eaids.development.dto.request;

import com.feadev.eaids.development.model.enums.GoalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGoalRequest {

    private String title;
    private String description;
    private String metric;
    private BigDecimal currentValue;
    private BigDecimal targetValue;
    private LocalDate targetDate;
    private GoalStatus status;
}
