package com.feadev.eaids.history.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoryTimelineEntry {

    private String type;
    private LocalDateTime date;
    private String summary;
    private Object data;
}
