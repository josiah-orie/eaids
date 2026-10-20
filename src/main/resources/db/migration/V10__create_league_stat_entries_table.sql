-- ============================================================
-- League stat entries: player stats scoped to league fixtures
-- ============================================================

CREATE TABLE IF NOT EXISTS league_stat_entries (
    id            BINARY(16)     NOT NULL,
    team_player_id BINARY(16)    NOT NULL,
    fixture_id    BINARY(16)     NOT NULL,
    definition_id BINARY(16)     NOT NULL,
    value         DECIMAL(10, 2) NOT NULL,
    recorded_by   BINARY(16),
    created_date  DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_lse_team_player  FOREIGN KEY (team_player_id) REFERENCES team_players (id),
    CONSTRAINT fk_lse_fixture      FOREIGN KEY (fixture_id)     REFERENCES match_fixtures (id),
    CONSTRAINT fk_lse_definition   FOREIGN KEY (definition_id)  REFERENCES match_stat_definitions (id),
    INDEX idx_lse_fixture          (fixture_id),
    INDEX idx_lse_team_player      (team_player_id),
    INDEX idx_lse_fixture_player   (fixture_id, team_player_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
