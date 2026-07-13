package com.feadev.eaids.evaluation.dto.response;

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
public class EvaluationResponse {

    private UUID id;
    private UUID enrollmentId;
    private UUID evaluatorId;
    private String evaluatorName;
    private BigDecimal technicalScore;
    private BigDecimal tacticalScore;
    private BigDecimal leadershipScore;
    private BigDecimal characterScore;
    private BigDecimal averageScore;
    private String recommendation;
    private String comments;
    private LocalDateTime createdDate;
}
