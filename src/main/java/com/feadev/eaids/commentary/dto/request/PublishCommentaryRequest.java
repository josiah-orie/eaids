package com.feadev.eaids.commentary.dto.request;

import com.feadev.eaids.commentary.model.enums.GameEventType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Payload for publishing a single game event to a live fixture stream.
 */
@Data
public class PublishCommentaryRequest {

    @NotNull(message = "eventType is required")
    private GameEventType eventType;

    /** Game clock in seconds. Optional — omit for non-play events. */
    private Integer eventTimeSeconds;

    /** Quarter or overtime period number. */
    private Integer quarter;

    /** Current home team score. Required for SCORE_UPDATE events. */
    private Integer homeScore;

    /** Current away team score. Required for SCORE_UPDATE events. */
    private Integer awayScore;

    /** Human-readable description or play-by-play text. */
    private String description;

    /** Required for PLAYER_EVENT type — references a TeamPlayer in the fixture. */
    private UUID playerTeamPlayerId;

    /** e.g. TD, INT, SACK, FUMBLE, PENALTY_FLAG */
    private String playerEventSubtype;
}
