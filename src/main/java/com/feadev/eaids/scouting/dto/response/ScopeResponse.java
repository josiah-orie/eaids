package com.feadev.eaids.scouting.dto.response;

import com.feadev.eaids.scouting.model.enums.ScopeType;
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
public class ScopeResponse {

    private UUID id;
    private ScopeType scopeType;
    private String scopeValue;
    private LocalDateTime grantedDate;
    private LocalDateTime expiresAt;
}
