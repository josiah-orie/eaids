package com.feadev.eaids.event.dto.response;

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
public class EventRegistrationResponse {

    private UUID id;
    private UUID eventId;
    private String eventName;
    private UUID userId;
    private String userEmail;
    private String guestName;
    private String guestEmail;
    private String guestPhone;
    private boolean guest;
    private String status;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
