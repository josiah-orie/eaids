package com.feadev.eaids.league.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerTransferResponse {
    private UUID id;
    private UUID athleteUserId;
    private UUID fromTeamId;
    private String fromTeamName;
    private UUID toTeamId;
    private String toTeamName;
    private String transferType;
    private LocalDate transferDate;
    private String notes;
    private String createdBy;
    private LocalDateTime createdDate;
}
