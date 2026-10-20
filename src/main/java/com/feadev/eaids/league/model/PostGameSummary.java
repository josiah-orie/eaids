package com.feadev.eaids.league.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Per-athlete, per-fixture post-game statistics snapshot.
 *
 * <p>Created (or upserted) automatically when a {@link MatchFixture} transitions
 * to {@link com.feadev.eaids.league.model.enums.FixtureStatus#COMPLETED}.
 * Raw data source is {@link LeagueStatEntry}; this entity is the pre-computed summary
 * for fast display without re-aggregating every time.
 *
 * <p>The unique constraint on {@code (fixture_id, team_player_id)} ensures each
 * player has exactly one summary row per fixture; re-running the computation
 * overwrites the existing row rather than creating a duplicate.
 */
@Entity
@Table(
    name = "post_game_summaries",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_pgs_fixture_player",
        columnNames = {"fixture_id", "team_player_id"}
    )
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PostGameSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fixture_id", nullable = false)
    private MatchFixture fixture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_player_id", nullable = false)
    private TeamPlayer teamPlayer;

    /** Denormalised from teamPlayer.athlete.id for efficient per-athlete queries. */
    @Column(name = "athlete_user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID athleteUserId;

    /** Denormalised from teamPlayer.team.id for efficient per-team queries. */
    @Column(name = "team_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID teamId;

    @Column(name = "total_passing_yards", precision = 10, scale = 2)
    private BigDecimal totalPassingYards;

    @Column(name = "total_rushing_yards", precision = 10, scale = 2)
    private BigDecimal totalRushingYards;

    @Column(name = "total_receiving_yards", precision = 10, scale = 2)
    private BigDecimal totalReceivingYards;

    @Column(name = "total_tackles", precision = 10, scale = 2)
    private BigDecimal totalTackles;

    @Column(name = "touchdowns", precision = 10, scale = 2)
    private BigDecimal touchdowns;

    @Column(name = "interceptions", precision = 10, scale = 2)
    private BigDecimal interceptions;

    @Column(name = "sacks", precision = 10, scale = 2)
    private BigDecimal sacks;

    @Column(name = "fumbles", precision = 10, scale = 2)
    private BigDecimal fumbles;

    @Column(name = "penalties", precision = 10, scale = 2)
    private BigDecimal penalties;

    /**
     * JSON blob for any stat definitions not covered by the typed columns above.
     * Stored as a raw JSON string; serialised/deserialised by the service layer.
     */
    @Column(name = "custom_stats", columnDefinition = "JSON")
    private String customStats;

    @Column(name = "computed_at", nullable = false)
    private LocalDateTime computedAt;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
}
