package com.feadev.eaids.commentary.model.enums;

/**
 * Structured types for match commentary events broadcast during a live fixture.
 */
public enum GameEventType {
    /** Home or away score changed. */
    SCORE_UPDATE,
    /** Free-text play-by-play or announcer commentary. */
    COMMENTARY,
    /** A quarter (or overtime period) has started. */
    QUARTER_START,
    /** A quarter (or overtime period) has ended. */
    QUARTER_END,
    /** A penalty was flagged. */
    PENALTY,
    /** A player-specific event (TD, INT, SACK, FUMBLE, etc.). */
    PLAYER_EVENT
}
