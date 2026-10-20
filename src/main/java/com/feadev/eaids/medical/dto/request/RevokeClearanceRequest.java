package com.feadev.eaids.medical.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokeClearanceRequest {

    @NotBlank(message = "Revocation reason is required")
    private String revocationReason;
}
