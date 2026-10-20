package com.feadev.eaids.league.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeagueRequest {

    @Size(max = 200, message = "League name must not exceed 200 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Min(value = 2000, message = "Season year must be 2000 or later")
    @Max(value = 2100, message = "Season year must be 2100 or earlier")
    private Integer seasonYear;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Size(max = 100, message = "Region must not exceed 100 characters")
    private String region;
}
