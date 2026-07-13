package com.feadev.eaids.registrations.dto.response;

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
public class AuditLogResponse {

    private UUID id;
    private String action;
    private UUID userId;
    private String actorIdentifier;
    private String ipAddress;
    private String userAgent;
    private String details;
    private LocalDateTime createdDate;
}
