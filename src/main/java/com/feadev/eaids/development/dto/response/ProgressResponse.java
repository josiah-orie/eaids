package com.feadev.eaids.development.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResponse {

    private UUID id;
    private UUID planId;
    private UUID goalId;
    private String goalTitle;
    private BigDecimal value;
    private String notes;
    private UUID recordedBy;
    private LocalDate recordedDate;
}
