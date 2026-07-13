package com.feadev.eaids.scouting.dto.response;

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
public class BookmarkResponse {

    private UUID id;
    private UUID scoutUserId;
    private UUID athleteUserId;
    private String athleteName;
    private String notes;
    private LocalDateTime createdDate;
}
