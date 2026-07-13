package com.feadev.eaids.videolibrary.dto.request;

import com.feadev.eaids.technical.model.enums.FootballPosition;
import com.feadev.eaids.videolibrary.model.enums.VideoCategory;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVideoRequest {

    @Size(max = 200)
    private String title;

    private String description;
    private VideoCategory category;
    private FootballPosition position;
    private UUID campId;
    private Boolean isPublic;
}
