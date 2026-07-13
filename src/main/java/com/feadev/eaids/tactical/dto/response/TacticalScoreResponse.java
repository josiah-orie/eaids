package com.feadev.eaids.tactical.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TacticalScoreResponse {

    private UUID id;
    private UUID enrollmentId;
    private UUID metricId;
    private String metricName;
    private int score;
    private int maxScore;
    private UUID evaluatorId;
    private LocalDateTime createdDate;
}
