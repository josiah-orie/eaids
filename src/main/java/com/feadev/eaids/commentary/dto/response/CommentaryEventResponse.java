package com.feadev.eaids.commentary.dto.response;

import com.feadev.eaids.commentary.model.enums.GameEventType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Read-model for a single commentary event, safe for public (unauthenticated) consumption.
 */
@Data
@Builder
public class CommentaryEventResponse {

    private UUID id;
    private UUID fixtureId;

    private GameEventType eventType;
    private Integer eventTimeSeconds;
    private Integer quarter;
    private Integer homeScore;
    private Integer awayScore;
    private String description;

    /** Null unless eventType == PLAYER_EVENT */
    private UUID playerTeamPlayerId;
    private String playerName;
    private Integer jerseyNumber;
    private String playerEventSubtype;

    private String createdBy;
    private LocalDateTime createdDate;
}
