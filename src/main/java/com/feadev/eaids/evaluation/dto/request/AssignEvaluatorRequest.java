package com.feadev.eaids.evaluation.dto.request;

import com.feadev.eaids.registrations.model.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignEvaluatorRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Role is required")
    private UserRole role;

    @Builder.Default
    private boolean isLead = false;
}
