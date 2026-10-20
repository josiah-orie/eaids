package com.feadev.eaids.league.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDivisionRequest {

    @Size(max = 100, message = "Division name must not exceed 100 characters")
    private String name;

    @Min(value = 1, message = "Tier rank must be at least 1")
    private Integer tierRank;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;
}
