package com.feadev.eaids.league.dto.request;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFixtureRequest {
    private LocalDateTime scheduledDate;
    private String venue;
    private UUID refereeUserId;
    private String notes;
}
