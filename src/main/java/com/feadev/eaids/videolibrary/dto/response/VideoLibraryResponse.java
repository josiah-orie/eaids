package com.feadev.eaids.videolibrary.dto.response;

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
public class VideoLibraryResponse {

    private UUID id;
    private UUID athleteUserId;
    private String athleteName;
    private String title;
    private String description;
    private String url;
    private String videoType;
    private String category;
    private String position;
    private UUID campId;
    private UUID uploadedBy;
    private boolean isPublic;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
