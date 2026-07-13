package com.feadev.eaids.videolibrary.dto.request;

import com.feadev.eaids.registrations.model.enums.VideoType;
import com.feadev.eaids.technical.model.enums.FootballPosition;
import com.feadev.eaids.videolibrary.model.enums.VideoCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateVideoRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    private String description;

    @NotBlank(message = "URL is required")
    @Size(max = 2048)
    private String url;

    @NotNull(message = "Video type is required")
    private VideoType videoType;

    @NotNull(message = "Category is required")
    private VideoCategory category;

    private FootballPosition position;
    private UUID campId;
}
