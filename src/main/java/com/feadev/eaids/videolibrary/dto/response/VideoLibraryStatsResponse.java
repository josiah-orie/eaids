package com.feadev.eaids.videolibrary.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLibraryStatsResponse {

    private long totalVideos;
    private List<CategoryCount> byCategory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryCount {
        private String category;
        private long count;
    }
}
