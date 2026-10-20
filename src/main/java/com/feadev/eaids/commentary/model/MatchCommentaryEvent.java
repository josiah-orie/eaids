package com.feadev.eaids.commentary.model;

import com.feadev.eaids.commentary.model.enums.GameEventType;
import com.feadev.eaids.league.model.MatchFixture;
import com.feadev.eaids.league.model.TeamPlayer;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Persisted record of a single game event during a live fixture.
 * These records are replayed to SSE subscribers that join mid-match,
 * and retained for post-match commentary history.
 */
@Entity
@Table(name = "match_commentary_events")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class MatchCommentaryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private MatchFixture fixture;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private GameEventType eventType;

    /** Game clock position in seconds when the event occurred. */
    @Column(name = "event_time_seconds")
    private Integer eventTimeSeconds;

    /** Quarter number (1-4 regular play, 5+ overtime). */
    @Column(name = "quarter")
    private Integer quarter;

    @Column(name = "home_score")
    private Integer homeScore;

    @Column(name = "away_score")
    private Integer awayScore;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Populated only for {@link GameEventType#PLAYER_EVENT}. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_team_player_id")
    private TeamPlayer playerTeamPlayer;

    /** e.g. TD, INT, SACK, FUMBLE, PENALTY_FLAG */
    @Column(name = "player_event_subtype", length = 50)
    private String playerEventSubtype;

    @Column(name = "created_by")
    private String createdBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
}
