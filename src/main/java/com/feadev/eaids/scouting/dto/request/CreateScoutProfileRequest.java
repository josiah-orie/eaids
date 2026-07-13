package com.feadev.eaids.scouting.dto.request;

import com.feadev.eaids.scouting.model.enums.OrganizationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a scout profile after account activation")
public class CreateScoutProfileRequest {

    @NotBlank(message = "Full name is required")
    @Schema(description = "Scout's full name", example = "Jane Okafor")
    private String fullName;

    @Schema(description = "Organization the scout represents", example = "NFL International")
    private String organization;

    @NotNull(message = "Organization type is required")
    @Schema(description = "Type of organization", example = "PROFESSIONAL")
    private OrganizationType organizationType;

    @Schema(description = "Country of operation", example = "Nigeria")
    private String country;

    @Schema(description = "Region of operation", example = "West Africa")
    private String region;
}
