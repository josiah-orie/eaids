package com.feadev.eaids.evaluation.dto.response;

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
public class EvaluatorResponse {

    private UUID id;
    private UUID campId;
    private UUID userId;
    private String role;
    private boolean isLead;
    private LocalDateTime createdDate;
}
