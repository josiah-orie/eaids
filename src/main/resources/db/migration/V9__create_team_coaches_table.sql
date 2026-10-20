-- ============================================================
-- Team coaches table
-- Each team can have exactly one HEAD_COACH and one ASSISTANT_COACH.
-- Reassignment is handled by updating the existing row (PUT).
-- ============================================================

CREATE TABLE IF NOT EXISTS team_coaches (
    id BINARY(16) NOT NULL,
    team_id BINARY(16) NOT NULL,
    coach_user_id BINARY(16) NOT NULL,
    role ENUM('HEAD_COACH', 'ASSISTANT_COACH') NOT NULL,
    assigned_date DATETIME(6) NOT NULL,
    notes TEXT,
    assigned_by VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_team_coach_role UNIQUE (team_id, role),
    CONSTRAINT fk_team_coaches_team FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT fk_team_coaches_user FOREIGN KEY (coach_user_id) REFERENCES users (id),
    INDEX idx_team_coaches_team (team_id),
    INDEX idx_team_coaches_user (coach_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
