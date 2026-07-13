package com.feadev.eaids.registrations.dto.request;

import com.feadev.eaids.registrations.model.enums.VideoType;
import com.feadev.eaids.registrations.validation.VideoUrl;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@VideoUrl
public class VideoAddRequest {

    @NotBlank(message = "Video URL is required")
    private String url;

    @NotNull(message = "Video type is required")
    private VideoType videoType;
}
