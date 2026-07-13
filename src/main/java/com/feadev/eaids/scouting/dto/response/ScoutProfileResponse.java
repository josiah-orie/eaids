package com.feadev.eaids.scouting.dto.response;

import com.feadev.eaids.scouting.model.enums.OrganizationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoutProfileResponse {

    private UUID id;
    private UUID userId;
    private String fullName;
    private String organization;
    private OrganizationType organizationType;
    private String country;
    private String region;
    private boolean verified;
    private List<ScopeResponse> scopes;
    private LocalDateTime createdDate;
}
