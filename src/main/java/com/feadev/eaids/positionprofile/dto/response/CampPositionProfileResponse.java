package com.feadev.eaids.positionprofile.dto.response;

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
public class CampPositionProfileResponse {

    private UUID id;
    private UUID campId;
    private UUID positionProfileId;
    private String positionProfileName;
    private String position;
    private LocalDateTime assignedDate;
}
