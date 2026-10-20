-- ============================================================
-- Feature: Live Commentary (SSE) + Post-Game Summaries
-- ============================================================

-- Match commentary events
-- Stores every structured game event broadcast during a live fixture.
CREATE TABLE IF NOT EXISTS match_commentary_events (
    id                      BINARY(16)   NOT NULL,
    fixture_id              BINARY(16)   NOT NULL,
    event_type              VARCHAR(50)  NOT NULL COMMENT 'SCORE_UPDATE | COMMENTARY | QUARTER_START | QUARTER_END | PENALTY | PLAYER_EVENT',
    event_time_seconds      INT                   COMMENT 'Game clock in seconds at the time of the event',
    quarter                 INT                   COMMENT '1-4 regular, 5+ overtime',
    home_score              INT,
    away_score              INT,
    description             TEXT,
    player_team_player_id   BINARY(16)            COMMENT 'nullable – only set for PLAYER_EVENT type',
    player_event_subtype    VARCHAR(50)           COMMENT 'TD | INT | SACK | FUMBLE | PENALTY_FLAG | etc.',
    created_by              VARCHAR(255),
    created_date            DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_mce_fixture       FOREIGN KEY (fixture_id)            REFERENCES match_fixtures (id),
    CONSTRAINT fk_mce_team_player   FOREIGN KEY (player_team_player_id) REFERENCES team_players (id),
    INDEX idx_mce_fixture       (fixture_id),
    INDEX idx_mce_created_date  (created_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Post-game athlete stat summaries
-- One row per (fixture, team_player); computed/upserted when a fixture transitions to COMPLETED.
CREATE TABLE IF NOT EXISTS post_game_summaries (
    id                      BINARY(16)      NOT NULL,
    fixture_id              BINARY(16)      NOT NULL,
    team_player_id          BINARY(16)      NOT NULL,
    athlete_user_id         BINARY(16)      NOT NULL  COMMENT 'Denormalised for fast per-athlete queries',
    team_id                 BINARY(16)      NOT NULL  COMMENT 'Denormalised for fast per-team queries',
    total_passing_yards     DECIMAL(10, 2),
    total_rushing_yards     DECIMAL(10, 2),
    total_receiving_yards   DECIMAL(10, 2),
    total_tackles           DECIMAL(10, 2),
    touchdowns              DECIMAL(10, 2),
    interceptions           DECIMAL(10, 2),
    sacks                   DECIMAL(10, 2),
    fumbles                 DECIMAL(10, 2),
    penalties               DECIMAL(10, 2),
    custom_stats            JSON                      COMMENT 'Overflow bucket for non-standard stat definitions',
    computed_at             DATETIME(6)     NOT NULL,
    created_date            DATETIME(6)     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_pgs_fixture_player  UNIQUE (fixture_id, team_player_id),
    CONSTRAINT fk_pgs_fixture         FOREIGN KEY (fixture_id)     REFERENCES match_fixtures (id),
    CONSTRAINT fk_pgs_team_player     FOREIGN KEY (team_player_id) REFERENCES team_players (id),
    CONSTRAINT fk_pgs_athlete         FOREIGN KEY (athlete_user_id) REFERENCES users (id),
    INDEX idx_pgs_fixture        (fixture_id),
    INDEX idx_pgs_athlete        (athlete_user_id),
    INDEX idx_pgs_team           (team_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
