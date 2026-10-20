-- ============================================================
-- League module tables
-- ============================================================

-- Leagues
CREATE TABLE IF NOT EXISTS leagues (
    id BINARY(16) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    season_year INT NOT NULL,
    country VARCHAR(100),
    region VARCHAR(100),
    status ENUM('DRAFT', 'ACTIVE', 'COMPLETED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    PRIMARY KEY (id),
    INDEX idx_leagues_status (status),
    INDEX idx_leagues_country (country),
    INDEX idx_leagues_season_year (season_year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- League divisions
CREATE TABLE IF NOT EXISTS league_divisions (
    id BINARY(16) NOT NULL,
    league_id BINARY(16) NOT NULL,
    name VARCHAR(100) NOT NULL,
    tier_rank INT NOT NULL,
    description TEXT,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_league_division_name UNIQUE (league_id, name),
    CONSTRAINT fk_league_divisions_league FOREIGN KEY (league_id) REFERENCES leagues (id),
    INDEX idx_league_divisions_league (league_id),
    INDEX idx_league_divisions_tier_rank (tier_rank)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- League staff assignments
CREATE TABLE IF NOT EXISTS league_staff (
    id BINARY(16) NOT NULL,
    league_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    staff_role ENUM('LEAGUE_COORDINATOR', 'REFEREE', 'STATISTICIAN', 'MEDIA_OFFICER', 'MEDICAL_OFFICER', 'SECURITY_OFFICER') NOT NULL,
    notes TEXT,
    assigned_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_league_staff_user_role UNIQUE (league_id, user_id, staff_role),
    CONSTRAINT fk_league_staff_league FOREIGN KEY (league_id) REFERENCES leagues (id),
    CONSTRAINT fk_league_staff_user FOREIGN KEY (user_id) REFERENCES users (id),
    INDEX idx_league_staff_league (league_id),
    INDEX idx_league_staff_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Teams
CREATE TABLE IF NOT EXISTS teams (
    id BINARY(16) NOT NULL,
    name VARCHAR(200) NOT NULL,
    league_id BINARY(16) NOT NULL,
    division_id BINARY(16),
    home_venue VARCHAR(200),
    country VARCHAR(100),
    state VARCHAR(100),
    logo_url VARCHAR(512),
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') NOT NULL DEFAULT 'ACTIVE',
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_team_name_league UNIQUE (name, league_id),
    CONSTRAINT fk_teams_league FOREIGN KEY (league_id) REFERENCES leagues (id),
    CONSTRAINT fk_teams_division FOREIGN KEY (division_id) REFERENCES league_divisions (id),
    INDEX idx_teams_league (league_id),
    INDEX idx_teams_division (division_id),
    INDEX idx_teams_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Team players (roster)
CREATE TABLE IF NOT EXISTS team_players (
    id BINARY(16) NOT NULL,
    team_id BINARY(16) NOT NULL,
    athlete_user_id BINARY(16) NOT NULL,
    jersey_number INT,
    position ENUM('QUARTERBACK', 'RUNNING_BACK', 'WIDE_RECEIVER', 'TIGHT_END', 'OFFENSIVE_LINE', 'DEFENSIVE_LINE', 'LINEBACKER', 'CORNERBACK', 'SAFETY', 'KICKER', 'PUNTER', 'RETURNER', 'ATHLETE') NOT NULL,
    status ENUM('ACTIVE', 'INJURED', 'SUSPENDED', 'TRANSFERRED') NOT NULL DEFAULT 'ACTIVE',
    drafted_date DATE NOT NULL,
    released_date DATE,
    PRIMARY KEY (id),
    CONSTRAINT uk_team_player UNIQUE (team_id, athlete_user_id),
    CONSTRAINT fk_team_players_team FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT fk_team_players_user FOREIGN KEY (athlete_user_id) REFERENCES users (id),
    INDEX idx_team_players_team (team_id),
    INDEX idx_team_players_athlete (athlete_user_id),
    INDEX idx_team_players_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Player transfer history
CREATE TABLE IF NOT EXISTS player_transfers (
    id BINARY(16) NOT NULL,
    athlete_user_id BINARY(16) NOT NULL,
    from_team_id BINARY(16),
    to_team_id BINARY(16),
    transfer_type ENUM('DRAFT', 'TRANSFER', 'RELEASE', 'LOAN') NOT NULL,
    transfer_date DATE NOT NULL,
    notes TEXT,
    created_by VARCHAR(255),
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_player_transfers_athlete FOREIGN KEY (athlete_user_id) REFERENCES users (id),
    CONSTRAINT fk_player_transfers_from_team FOREIGN KEY (from_team_id) REFERENCES teams (id),
    CONSTRAINT fk_player_transfers_to_team FOREIGN KEY (to_team_id) REFERENCES teams (id),
    INDEX idx_player_transfers_athlete (athlete_user_id),
    INDEX idx_player_transfers_date (transfer_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Match fixtures
CREATE TABLE IF NOT EXISTS match_fixtures (
    id BINARY(16) NOT NULL,
    league_id BINARY(16) NOT NULL,
    division_id BINARY(16),
    home_team_id BINARY(16) NOT NULL,
    away_team_id BINARY(16) NOT NULL,
    scheduled_date DATETIME(6) NOT NULL,
    venue VARCHAR(200),
    status ENUM('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'POSTPONED', 'CANCELLED') NOT NULL DEFAULT 'SCHEDULED',
    home_score INT,
    away_score INT,
    referee_user_id BINARY(16),
    notes TEXT,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT fk_fixtures_league FOREIGN KEY (league_id) REFERENCES leagues (id),
    CONSTRAINT fk_fixtures_division FOREIGN KEY (division_id) REFERENCES league_divisions (id),
    CONSTRAINT fk_fixtures_home_team FOREIGN KEY (home_team_id) REFERENCES teams (id),
    CONSTRAINT fk_fixtures_away_team FOREIGN KEY (away_team_id) REFERENCES teams (id),
    CONSTRAINT fk_fixtures_referee FOREIGN KEY (referee_user_id) REFERENCES users (id),
    INDEX idx_fixtures_league (league_id),
    INDEX idx_fixtures_division (division_id),
    INDEX idx_fixtures_home_team (home_team_id),
    INDEX idx_fixtures_away_team (away_team_id),
    INDEX idx_fixtures_status (status),
    INDEX idx_fixtures_scheduled_date (scheduled_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
