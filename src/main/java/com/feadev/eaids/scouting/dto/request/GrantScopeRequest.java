package com.feadev.eaids.scouting.dto.request;

import com.feadev.eaids.scouting.model.enums.ScopeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrantScopeRequest {

    @NotNull(message = "Scope type is required")
    private ScopeType scopeType;

    @NotBlank(message = "Scope value is required")
    private String scopeValue;

    private LocalDateTime expiresAt;
}
