-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BINARY(16) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ROLE_ATHLETE', 'ROLE_COACH', 'ROLE_ADMIN', 'ROLE_SCOUT') NOT NULL,
    status ENUM('PENDING_VERIFICATION', 'PENDING_CONSENT', 'UNDER_REVIEW', 'ACTIVE', 'REJECTED', 'SUSPENDED', 'ARCHIVED') NOT NULL,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    phone_verified BOOLEAN NOT NULL DEFAULT FALSE,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_phone_number UNIQUE (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Athlete profiles
CREATE TABLE IF NOT EXISTS athlete_profiles (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    date_of_birth DATE NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    state_of_residence VARCHAR(100) NOT NULL,
    country_of_residence VARCHAR(100) NOT NULL,
    nationality VARCHAR(100) NOT NULL,
    height_cm DECIMAL(5,1),
    weight_kg DECIMAL(5,1),
    athlete_interest ENUM('TACKLE_FOOTBALL', 'FLAG_FOOTBALL', 'BOTH') NOT NULL,
    disability_status BOOLEAN NOT NULL DEFAULT FALSE,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_athlete_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_athlete_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Athlete sports backgrounds
CREATE TABLE IF NOT EXISTS athlete_sports_backgrounds (
    id BIGINT NOT NULL AUTO_INCREMENT,
    athlete_profile_id BINARY(16) NOT NULL,
    sports_background ENUM('ATHLETICS', 'BASKETBALL', 'RUGBY', 'WRESTLING', 'FOOTBALL_SOCCER', 'OTHER') NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_athlete_sports_backgrounds_profile FOREIGN KEY (athlete_profile_id) REFERENCES athlete_profiles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Athlete videos
CREATE TABLE IF NOT EXISTS athlete_videos (
    id BINARY(16) NOT NULL,
    athlete_profile_id BINARY(16) NOT NULL,
    url VARCHAR(2048) NOT NULL,
    video_type ENUM('HUDL', 'YOUTUBE', 'GOOGLE_DRIVE') NOT NULL,
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_athlete_videos_profile FOREIGN KEY (athlete_profile_id) REFERENCES athlete_profiles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Coach profiles
CREATE TABLE IF NOT EXISTS coach_profiles (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    gender ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    date_of_birth DATE NOT NULL,
    nationality VARCHAR(100) NOT NULL,
    state_of_residence VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    email_address VARCHAR(255),
    current_sport VARCHAR(100),
    current_organization VARCHAR(100),
    current_position VARCHAR(100),
    years_of_experience INT,
    area_of_interest ENUM('COACH', 'REFEREE', 'MEDICAL', 'PHYSIO', 'MEDIA') NOT NULL,
    availability ENUM('PART_TIME', 'FULL_TIME') NOT NULL,
    american_football_experience ENUM('FLAG_FOOTBALL', 'TACKLE_FOOTBALL', 'NO_EXPERIENCE') NOT NULL,
    involvement_level ENUM('LOCAL', 'REGIONAL', 'NATIONAL', 'INTERNATIONAL') NOT NULL,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_coach_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_coach_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Coach coaching interests
CREATE TABLE IF NOT EXISTS coach_coaching_interests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    coach_profile_id BINARY(16) NOT NULL,
    coaching_interest ENUM('YOUTH_DEVELOPMENT', 'FLAG_FOOTBALL', 'TACKLE_FOOTBALL', 'STRENGTH_AND_CONDITIONING', 'OFFENSIVE_COACHING', 'DEFENSIVE_COACHING', 'SPECIAL_TEAMS', 'ATHLETE_DEVELOPMENT', 'SCOUTING') NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_coach_coaching_interests_profile FOREIGN KEY (coach_profile_id) REFERENCES coach_profiles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Coach documents
CREATE TABLE IF NOT EXISTS coach_documents (
    id BINARY(16) NOT NULL,
    coach_profile_id BINARY(16) NOT NULL,
    document_type ENUM('COACHING_CERTIFICATION', 'SPORTS_CERTIFICATION', 'FIRST_AID_CERTIFICATION', 'PROFESSIONAL_CERTIFICATION', 'NATIONAL_ID') NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(512) NOT NULL,
    file_size BIGINT NOT NULL,
    verification_status ENUM('PENDING', 'VERIFIED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    uploaded_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_coach_documents_profile FOREIGN KEY (coach_profile_id) REFERENCES coach_profiles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Email verification tokens
CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    token VARCHAR(255) NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at DATETIME(6) NOT NULL,
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_email_verification_tokens_token UNIQUE (token),
    CONSTRAINT fk_email_verification_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Refresh tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    token VARCHAR(512) NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at DATETIME(6) NOT NULL,
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Password reset tokens
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    token VARCHAR(255) NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at DATETIME(6) NOT NULL,
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_password_reset_tokens_token UNIQUE (token),
    CONSTRAINT fk_password_reset_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- OTP tokens
CREATE TABLE IF NOT EXISTS otp_tokens (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    otp_code VARCHAR(6) NOT NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at DATETIME(6) NOT NULL,
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_otp_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Guardian consents
CREATE TABLE IF NOT EXISTS guardian_consents (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    guardian_name VARCHAR(100) NOT NULL,
    guardian_email VARCHAR(255) NOT NULL,
    guardian_phone VARCHAR(20) NOT NULL,
    consent_accepted BOOLEAN NOT NULL,
    electronic_signature VARCHAR(500) NOT NULL,
    consent_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_guardian_consents_user_id UNIQUE (user_id),
    CONSTRAINT fk_guardian_consents_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Audit logs  action ENUM('REGISTRATION', 'LOGIN_SUCCESS', 'LOGIN_FAILURE', 'LOGOUT', 'PASSWORD_CHANGE', 'PROFILE_UPDATE', 'EMAIL_VERIFICATION', 'COACH_APPROVAL', 'ATHLETE_APPROVAL', 'CAMP_CREATED', 'CAMP_UPDATED', 'CAMP_STATUS_CHANGED', 'CAMP_DELETED', 'ATHLETE_ENROLLED', 'ENROLLMENT_STATUS_CHANGED', 'ATTENDANCE_RECORDED', 'DISCIPLINE_RECORDED', 'MEASUREMENT_RECORDED', 'TECHNICAL_SCORE_RECORDED', 'TACTICAL_SCORE_RECORDED', 'EVALUATION_RECORDED', 'SCORES_CALCULATED', 'RANKINGS_GENERATED', 'SELECTION_GENERATED', 'SELECTION_APPROVED', 'SELECTION_REJECTED', 'TRAINING_LOAD_RECORDED', 'POSITION_RECOMMENDED', 'POTENTIAL_PREDICTED', 'INJURY_RISK_ASSESSED', 'NATIONAL_RANKING_GENERATED', 'BENCHMARK_CREATED', 'BENCHMARK_UPDATED', 'REPORT_DOWNLOADED', 'INTERVIEW_REQUESTED', 'INTERVIEW_APPROVED', 'INTERVIEW_REJECTED', 'SCOUT_SCOPE_GRANTED', 'SCOUT_SCOPE_REVOKED') NOT NULL,
CREATE TABLE IF NOT EXISTS audit_logs (
    id BINARY(16) NOT NULL,
    action VARCHAR(100) NOT NULL,
    user_id BINARY(16),
    actor_identifier VARCHAR(255) NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(512),
    details TEXT,
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_audit_logs_action (action),
    INDEX idx_audit_logs_user_id (user_id),
    INDEX idx_audit_logs_created_date (created_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Camps
CREATE TABLE IF NOT EXISTS camps (
    id BINARY(16) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    camp_type ENUM('IDENTIFICATION', 'DEVELOPMENT', 'ELITE_SELECTION', 'REGIONAL', 'NATIONAL', 'INTERNATIONAL') NOT NULL,
    status ENUM('DRAFT', 'REGISTRATION_OPEN', 'REGISTRATION_CLOSED', 'IN_PROGRESS', 'COMPLETED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT',
    age_category ENUM('UNDER_14', 'UNDER_16', 'UNDER_18', 'UNDER_21', 'OPEN') NOT NULL,
    location VARCHAR(200) NOT NULL,
    country VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    registration_deadline DATE,
    capacity INT NOT NULL,
    current_enrollment INT NOT NULL DEFAULT 0,
    aggregation_strategy ENUM('AVERAGE', 'WEIGHTED_AVERAGE', 'MEDIAN') NOT NULL DEFAULT 'AVERAGE',
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Camp enrollments
CREATE TABLE IF NOT EXISTS camp_enrollments (
    id BINARY(16) NOT NULL,
    camp_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'WAITLISTED', 'CANCELLED', 'WITHDRAWN', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    enrollment_number INT,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_enrollment_camp_user UNIQUE (camp_id, user_id),
    CONSTRAINT fk_enrollment_camp FOREIGN KEY (camp_id) REFERENCES camps (id),
    CONSTRAINT fk_enrollment_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Camp stages (assessment configuration)
CREATE TABLE IF NOT EXISTS camp_stages (
    id BINARY(16) NOT NULL,
    camp_id BINARY(16) NOT NULL,
    stage_name VARCHAR(100) NOT NULL,
    stage_order INT NOT NULL,
    description TEXT,
    duration_days INT,
    score_weight DECIMAL(5,2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_camp_stages_camp FOREIGN KEY (camp_id) REFERENCES camps (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Attendance records
CREATE TABLE IF NOT EXISTS attendance_records (
    id BINARY(16) NOT NULL,
    enrollment_id BINARY(16) NOT NULL,
    camp_id BINARY(16) NOT NULL,
    attendance_date DATE NOT NULL,
    session_type ENUM('MORNING', 'AFTERNOON', 'EVENING') NOT NULL,
    status ENUM('PRESENT', 'LATE', 'EXCUSED', 'ABSENT') NOT NULL,
    score DECIMAL(5,2),
    recorded_by VARCHAR(255),
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_attendance_enrollment_date_session UNIQUE (enrollment_id, attendance_date, session_type),
    CONSTRAINT fk_attendance_enrollment FOREIGN KEY (enrollment_id) REFERENCES camp_enrollments (id),
    CONSTRAINT fk_attendance_camp FOREIGN KEY (camp_id) REFERENCES camps (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Discipline records
CREATE TABLE IF NOT EXISTS discipline_records (
    id BINARY(16) NOT NULL,
    enrollment_id BINARY(16) NOT NULL,
    camp_id BINARY(16) NOT NULL,
    infraction_type ENUM('WARNING', 'YELLOW_CARD', 'RED_CARD', 'SUSPENSION') NOT NULL,
    penalty_points INT NOT NULL,
    description TEXT,
    infraction_date DATE NOT NULL,
    recorded_by VARCHAR(255),
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_discipline_enrollment FOREIGN KEY (enrollment_id) REFERENCES camp_enrollments (id),
    CONSTRAINT fk_discipline_camp FOREIGN KEY (camp_id) REFERENCES camps (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Assessment metrics (physical)
CREATE TABLE IF NOT EXISTS assessment_metrics (
    id BINARY(16) NOT NULL,
    camp_id BINARY(16) NOT NULL,
    category ENUM('SPEED', 'AGILITY', 'STRENGTH', 'ENDURANCE', 'FLEXIBILITY') NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    unit ENUM('SECONDS', 'METERS', 'KILOGRAMS', 'REPETITIONS', 'CENTIMETERS') NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    lower_is_better BOOLEAN NOT NULL DEFAULT FALSE,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_assessment_metrics_camp FOREIGN KEY (camp_id) REFERENCES camps (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Metric benchmarks
CREATE TABLE IF NOT EXISTS metric_benchmarks (
    id BINARY(16) NOT NULL,
    metric_id BINARY(16) NOT NULL,
    min_value DECIMAL(10,3),
    max_value DECIMAL(10,3),
    score INT NOT NULL,
    age_category ENUM('UNDER_14', 'UNDER_16', 'UNDER_18', 'UNDER_21', 'OPEN'),
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    PRIMARY KEY (id),
    CONSTRAINT fk_metric_benchmarks_metric FOREIGN KEY (metric_id) REFERENCES assessment_metrics (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Athlete measurements
CREATE TABLE IF NOT EXISTS athlete_measurements (
    id BINARY(16) NOT NULL,
    enrollment_id BINARY(16) NOT NULL,
    metric_id BINARY(16) NOT NULL,
    raw_value DECIMAL(10,3) NOT NULL,
    calculated_score INT,
    measured_date DATE NOT NULL,
    recorded_by VARCHAR(255),
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_athlete_measurements_enrollment FOREIGN KEY (enrollment_id) REFERENCES camp_enrollments (id),
    CONSTRAINT fk_athlete_measurements_metric FOREIGN KEY (metric_id) REFERENCES assessment_metrics (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Technical metrics (position-specific)
CREATE TABLE IF NOT EXISTS technical_metrics (
    id BINARY(16) NOT NULL,
    camp_id BINARY(16) NOT NULL,
    position ENUM('QUARTERBACK', 'RUNNING_BACK', 'WIDE_RECEIVER', 'TIGHT_END', 'OFFENSIVE_LINE', 'DEFENSIVE_LINE', 'LINEBACKER', 'CORNERBACK', 'SAFETY', 'KICKER', 'PUNTER', 'RETURNER', 'ATHLETE') NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    max_score INT NOT NULL,
    weight DECIMAL(5,2),
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_technical_metrics_camp FOREIGN KEY (camp_id) REFERENCES camps (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Technical scores
CREATE TABLE IF NOT EXISTS technical_scores (
    id BINARY(16) NOT NULL,
    enrollment_id BINARY(16) NOT NULL,
    metric_id BINARY(16) NOT NULL,
    score INT NOT NULL,
    evaluator_id BINARY(16),
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_technical_scores_enrollment FOREIGN KEY (enrollment_id) REFERENCES camp_enrollments (id),
    CONSTRAINT fk_technical_scores_metric FOREIGN KEY (metric_id) REFERENCES technical_metrics (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tactical metrics (camp-level, not position-specific)
CREATE TABLE IF NOT EXISTS tactical_metrics (
    id BINARY(16) NOT NULL,
    camp_id BINARY(16) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    max_score INT NOT NULL,
    weight DECIMAL(5,2),
    description TEXT,
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_tactical_metrics_camp FOREIGN KEY (camp_id) REFERENCES camps (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tactical scores
CREATE TABLE IF NOT EXISTS tactical_scores (
    id BINARY(16) NOT NULL,
    enrollment_id BINARY(16) NOT NULL,
    metric_id BINARY(16) NOT NULL,
    score INT NOT NULL,
    evaluator_id BINARY(16),
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_tactical_scores_enrollment FOREIGN KEY (enrollment_id) REFERENCES camp_enrollments (id),
    CONSTRAINT fk_tactical_scores_metric FOREIGN KEY (metric_id) REFERENCES tactical_metrics (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Camp evaluators
CREATE TABLE IF NOT EXISTS camp_evaluators (
    id BINARY(16) NOT NULL,
    camp_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    role ENUM('ROLE_COACH', 'ROLE_SCOUT') NOT NULL,
    is_lead BOOLEAN NOT NULL DEFAULT FALSE,
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_evaluator_camp_user UNIQUE (camp_id, user_id),
    CONSTRAINT fk_evaluators_camp FOREIGN KEY (camp_id) REFERENCES camps (id),
    CONSTRAINT fk_evaluators_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Coach evaluations
CREATE TABLE IF NOT EXISTS coach_evaluations (
    id BINARY(16) NOT NULL,
    enrollment_id BINARY(16) NOT NULL,
    evaluator_id BINARY(16) NOT NULL,
    technical_score DECIMAL(5,2),
    tactical_score DECIMAL(5,2),
    leadership_score DECIMAL(5,2),
    character_score DECIMAL(5,2),
    recommendation TEXT,
    comments TEXT,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_evaluation_enrollment_evaluator UNIQUE (enrollment_id, evaluator_id),
    CONSTRAINT fk_evaluations_enrollment FOREIGN KEY (enrollment_id) REFERENCES camp_enrollments (id),
    CONSTRAINT fk_evaluations_evaluator FOREIGN KEY (evaluator_id) REFERENCES camp_evaluators (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Camp weight profiles
CREATE TABLE IF NOT EXISTS camp_weight_profiles (
    id BINARY(16) NOT NULL,
    camp_id BINARY(16) NOT NULL,
    position ENUM('QUARTERBACK', 'RUNNING_BACK', 'WIDE_RECEIVER', 'TIGHT_END', 'OFFENSIVE_LINE', 'DEFENSIVE_LINE', 'LINEBACKER', 'CORNERBACK', 'SAFETY', 'KICKER', 'PUNTER', 'RETURNER', 'ATHLETE') NOT NULL,
    physical_weight DECIMAL(5,2) NOT NULL DEFAULT 20.00,
    technical_weight DECIMAL(5,2) NOT NULL DEFAULT 25.00,
    tactical_weight DECIMAL(5,2) NOT NULL DEFAULT 20.00,
    attendance_weight DECIMAL(5,2) NOT NULL DEFAULT 5.00,
    discipline_weight DECIMAL(5,2) NOT NULL DEFAULT 5.00,
    coach_review_weight DECIMAL(5,2) NOT NULL DEFAULT 25.00,
    match_stats_weight DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_weight_profile_camp_position UNIQUE (camp_id, position),
    CONSTRAINT fk_weight_profiles_camp FOREIGN KEY (camp_id) REFERENCES camps (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Athlete scores (calculated)
CREATE TABLE IF NOT EXISTS athlete_scores (
    id BINARY(16) NOT NULL,
    enrollment_id BINARY(16) NOT NULL,
    camp_id BINARY(16) NOT NULL,
    position ENUM('QUARTERBACK', 'RUNNING_BACK', 'WIDE_RECEIVER', 'TIGHT_END', 'OFFENSIVE_LINE', 'DEFENSIVE_LINE', 'LINEBACKER', 'CORNERBACK', 'SAFETY', 'KICKER', 'PUNTER', 'RETURNER', 'ATHLETE') NOT NULL,
    physical_score DECIMAL(5,2),
    technical_score DECIMAL(5,2),
    tactical_score DECIMAL(5,2),
    attendance_score DECIMAL(5,2),
    discipline_score DECIMAL(5,2),
    coach_review_score DECIMAL(5,2),
    match_stats_score DECIMAL(5,2),
    total_weighted_score DECIMAL(5,2),
    calculated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_athlete_score_enrollment UNIQUE (enrollment_id),
    CONSTRAINT fk_athlete_scores_enrollment FOREIGN KEY (enrollment_id) REFERENCES camp_enrollments (id),
    CONSTRAINT fk_athlete_scores_camp FOREIGN KEY (camp_id) REFERENCES camps (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Athlete rankings
CREATE TABLE IF NOT EXISTS athlete_rankings (
    id BINARY(16) NOT NULL,
    enrollment_id BINARY(16) NOT NULL,
    camp_id BINARY(16) NOT NULL,
    ranking_type ENUM('OVERALL', 'POSITION', 'GENDER', 'COUNTRY', 'STATE') NOT NULL,
    rank_value INT NOT NULL,
    total_in_category INT NOT NULL,
    percentile DECIMAL(5,2),
    score DECIMAL(5,2),
    filter_value VARCHAR(100),
    calculated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_athlete_rankings_enrollment FOREIGN KEY (enrollment_id) REFERENCES camp_enrollments (id),
    CONSTRAINT fk_athlete_rankings_camp FOREIGN KEY (camp_id) REFERENCES camps (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Selection decisions
CREATE TABLE IF NOT EXISTS selection_decisions (
    id BINARY(16) NOT NULL,
    enrollment_id BINARY(16) NOT NULL,
    camp_id BINARY(16) NOT NULL,
    recommended_tier ENUM('TIER_1_ELITE', 'TIER_2_ADVANCED', 'TIER_3_DEVELOPING', 'TIER_4_POTENTIAL', 'TIER_5_NOT_SELECTED') NOT NULL,
    final_tier ENUM('TIER_1_ELITE', 'TIER_2_ADVANCED', 'TIER_3_DEVELOPING', 'TIER_4_POTENTIAL', 'TIER_5_NOT_SELECTED'),
    approval_status ENUM('PENDING', 'APPROVED', 'REJECTED', 'DEFERRED') NOT NULL DEFAULT 'PENDING',
    reviewed_by BINARY(16),
    comments TEXT,
    approval_date DATETIME(6),
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_selection_enrollment UNIQUE (enrollment_id),
    CONSTRAINT fk_selections_enrollment FOREIGN KEY (enrollment_id) REFERENCES camp_enrollments (id),
    CONSTRAINT fk_selections_camp FOREIGN KEY (camp_id) REFERENCES camps (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Admin profiles
CREATE TABLE IF NOT EXISTS admin_profiles (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    profile_image_url VARCHAR(512),
    job_title VARCHAR(150) NOT NULL,
    department VARCHAR(100) NOT NULL,
    region VARCHAR(100),
    country_managed VARCHAR(100),
    phone_extension VARCHAR(20),
    permissions_scope ENUM('SUPER_ADMIN_I', 'SUPER_ADMIN_II', 'DIRECTOR_OPERATIONS', 'DIRECTOR_FOOTBALL_DEV', 'DIRECTOR_SCOUTING', 'DIRECTOR_ACADEMY', 'DIRECTOR_WELFARE', 'DIRECTOR_ADAPTIVE', 'DIRECTOR_MEDIA', 'DIRECTOR_COMMERCIAL', 'DIRECTOR_FINANCE') NOT NULL,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_admin_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_admin_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Position profiles (global templates per football position)
CREATE TABLE IF NOT EXISTS position_profiles (
    id BINARY(16) NOT NULL,
    position ENUM('QUARTERBACK', 'RUNNING_BACK', 'WIDE_RECEIVER', 'TIGHT_END', 'OFFENSIVE_LINE', 'DEFENSIVE_LINE', 'LINEBACKER', 'CORNERBACK', 'SAFETY', 'KICKER', 'PUNTER', 'RETURNER', 'ATHLETE') NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Position scoring metrics
CREATE TABLE IF NOT EXISTS position_scoring_metrics (
    id BINARY(16) NOT NULL,
    position_profile_id BINARY(16) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    max_score INT NOT NULL,
    weight DECIMAL(5,2) NOT NULL,
    category VARCHAR(50),
    description TEXT,
    display_order INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_scoring_metrics_profile FOREIGN KEY (position_profile_id) REFERENCES position_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Position weight templates
CREATE TABLE IF NOT EXISTS position_weight_templates (
    id BINARY(16) NOT NULL,
    position_profile_id BINARY(16) NOT NULL,
    physical_weight DECIMAL(5,2) NOT NULL DEFAULT 20.00,
    technical_weight DECIMAL(5,2) NOT NULL DEFAULT 25.00,
    tactical_weight DECIMAL(5,2) NOT NULL DEFAULT 20.00,
    attendance_weight DECIMAL(5,2) NOT NULL DEFAULT 5.00,
    discipline_weight DECIMAL(5,2) NOT NULL DEFAULT 5.00,
    coach_review_weight DECIMAL(5,2) NOT NULL DEFAULT 25.00,
    match_stats_weight DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    PRIMARY KEY (id),
    CONSTRAINT uk_weight_templates_profile UNIQUE (position_profile_id),
    CONSTRAINT fk_weight_templates_profile FOREIGN KEY (position_profile_id) REFERENCES position_profiles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Assessment templates (reusable, linked to position profiles)
CREATE TABLE IF NOT EXISTS assessment_templates (
    id BINARY(16) NOT NULL,
    position_profile_id BINARY(16) NOT NULL,
    name VARCHAR(200) NOT NULL,
    age_category ENUM('UNDER_14', 'UNDER_16', 'UNDER_18', 'UNDER_21', 'OPEN') NOT NULL,
    pass_threshold DECIMAL(5,2) NOT NULL,
    scoring_scale_max INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_assessment_templates_profile FOREIGN KEY (position_profile_id) REFERENCES position_profiles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Assessment template metrics
CREATE TABLE IF NOT EXISTS assessment_template_metrics (
    id BINARY(16) NOT NULL,
    template_id BINARY(16) NOT NULL,
    metric_name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    unit VARCHAR(30),
    weight DECIMAL(5,2) NOT NULL,
    max_score INT NOT NULL,
    display_order INT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_template_metrics_template FOREIGN KEY (template_id) REFERENCES assessment_templates (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Camp position profiles (join table linking camps to position profiles)
CREATE TABLE IF NOT EXISTS camp_position_profiles (
    id BINARY(16) NOT NULL,
    camp_id BINARY(16) NOT NULL,
    position_profile_id BINARY(16) NOT NULL,
    assigned_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_camp_position_profile UNIQUE (camp_id, position_profile_id),
    CONSTRAINT fk_camp_pos_profiles_camp FOREIGN KEY (camp_id) REFERENCES camps (id),
    CONSTRAINT fk_camp_pos_profiles_profile FOREIGN KEY (position_profile_id) REFERENCES position_profiles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Match stat definitions (global stat types per position group)
CREATE TABLE IF NOT EXISTS match_stat_definitions (
    id BINARY(16) NOT NULL,
    position_group ENUM('QUARTERBACK', 'RECEIVER', 'DEFENSE', 'SPECIAL_TEAMS') NOT NULL,
    stat_name VARCHAR(100) NOT NULL,
    unit VARCHAR(30),
    display_order INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Match stat entries (individual game stat recordings)
CREATE TABLE IF NOT EXISTS match_stat_entries (
    id BINARY(16) NOT NULL,
    enrollment_id BINARY(16) NOT NULL,
    camp_id BINARY(16) NOT NULL,
    definition_id BINARY(16) NOT NULL,
    value DECIMAL(10,2) NOT NULL,
    match_date DATE NOT NULL,
    opponent VARCHAR(200),
    recorded_by BINARY(16),
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_match_stat_entries_enrollment FOREIGN KEY (enrollment_id) REFERENCES camp_enrollments (id),
    CONSTRAINT fk_match_stat_entries_camp FOREIGN KEY (camp_id) REFERENCES camps (id),
    CONSTRAINT fk_match_stat_entries_definition FOREIGN KEY (definition_id) REFERENCES match_stat_definitions (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Development plans
CREATE TABLE IF NOT EXISTS development_plans (
    id BINARY(16) NOT NULL,
    athlete_user_id BINARY(16) NOT NULL,
    camp_id BINARY(16) NOT NULL,
    coach_user_id BINARY(16),
    status ENUM('DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT',
    start_date DATE,
    target_end_date DATE,
    overall_assessment TEXT,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_dev_plans_athlete FOREIGN KEY (athlete_user_id) REFERENCES users (id),
    CONSTRAINT fk_dev_plans_camp FOREIGN KEY (camp_id) REFERENCES camps (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Development strengths
CREATE TABLE IF NOT EXISTS development_strengths (
    id BINARY(16) NOT NULL,
    plan_id BINARY(16) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    score DECIMAL(5,2),
    PRIMARY KEY (id),
    CONSTRAINT fk_dev_strengths_plan FOREIGN KEY (plan_id) REFERENCES development_plans (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Development weaknesses
CREATE TABLE IF NOT EXISTS development_weaknesses (
    id BINARY(16) NOT NULL,
    plan_id BINARY(16) NOT NULL,
    category VARCHAR(50) NOT NULL,
    description VARCHAR(255) NOT NULL,
    score DECIMAL(5,2),
    target_score VARCHAR(10),
    PRIMARY KEY (id),
    CONSTRAINT fk_dev_weaknesses_plan FOREIGN KEY (plan_id) REFERENCES development_plans (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Development goals
CREATE TABLE IF NOT EXISTS development_goals (
    id BINARY(16) NOT NULL,
    plan_id BINARY(16) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    metric VARCHAR(100),
    current_value DECIMAL(10,2),
    target_value DECIMAL(10,2),
    target_date DATE,
    status ENUM('NOT_STARTED', 'IN_PROGRESS', 'ACHIEVED', 'MISSED') NOT NULL DEFAULT 'NOT_STARTED',
    PRIMARY KEY (id),
    CONSTRAINT fk_dev_goals_plan FOREIGN KEY (plan_id) REFERENCES development_plans (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Development progress
CREATE TABLE IF NOT EXISTS development_progress (
    id BINARY(16) NOT NULL,
    plan_id BINARY(16) NOT NULL,
    goal_id BINARY(16),
    value DECIMAL(10,2),
    notes TEXT,
    recorded_by BINARY(16),
    recorded_date DATE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_dev_progress_plan FOREIGN KEY (plan_id) REFERENCES development_plans (id) ON DELETE CASCADE,
    CONSTRAINT fk_dev_progress_goal FOREIGN KEY (goal_id) REFERENCES development_goals (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Training recommendations (configurable bank)
CREATE TABLE IF NOT EXISTS training_recommendations (
    id BINARY(16) NOT NULL,
    position ENUM('QUARTERBACK', 'RUNNING_BACK', 'WIDE_RECEIVER', 'TIGHT_END', 'OFFENSIVE_LINE', 'DEFENSIVE_LINE', 'LINEBACKER', 'CORNERBACK', 'SAFETY', 'KICKER', 'PUNTER', 'RETURNER', 'ATHLETE') NOT NULL,
    category VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    difficulty_level VARCHAR(20) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Plan recommendations (join between plans and training recommendations)
CREATE TABLE IF NOT EXISTS plan_recommendations (
    id BINARY(16) NOT NULL,
    plan_id BINARY(16) NOT NULL,
    recommendation_id BINARY(16) NOT NULL,
    priority INT NOT NULL,
    coach_notes TEXT,
    PRIMARY KEY (id),
    CONSTRAINT fk_plan_recs_plan FOREIGN KEY (plan_id) REFERENCES development_plans (id) ON DELETE CASCADE,
    CONSTRAINT fk_plan_recs_rec FOREIGN KEY (recommendation_id) REFERENCES training_recommendations (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Video library items (centralized, searchable video repository)
CREATE TABLE IF NOT EXISTS video_library_items (
    id BINARY(16) NOT NULL,
    athlete_user_id BINARY(16) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    url VARCHAR(2048) NOT NULL,
    video_type ENUM('HUDL', 'YOUTUBE', 'GOOGLE_DRIVE') NOT NULL,
    category ENUM('DRILL', 'MATCH', 'CAMP_HIGHLIGHT', 'POSITION_SPECIFIC', 'TRAINING', 'OTHER') NOT NULL,
    position ENUM('QUARTERBACK', 'RUNNING_BACK', 'WIDE_RECEIVER', 'TIGHT_END', 'OFFENSIVE_LINE', 'DEFENSIVE_LINE', 'LINEBACKER', 'CORNERBACK', 'SAFETY', 'KICKER', 'PUNTER', 'RETURNER', 'ATHLETE'),
    camp_id BINARY(16),
    uploaded_by BINARY(16) NOT NULL,
    is_public BOOLEAN NOT NULL DEFAULT TRUE,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_video_library_athlete FOREIGN KEY (athlete_user_id) REFERENCES users (id),
    CONSTRAINT fk_video_library_camp FOREIGN KEY (camp_id) REFERENCES camps (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- PHASE 3: Analytics & International Scouting Tables
-- =====================================================

-- Training load records
CREATE TABLE IF NOT EXISTS training_load_records (
    id BINARY(16) NOT NULL,
    enrollment_id BINARY(16) NOT NULL,
    camp_id BINARY(16) NOT NULL,
    session_date DATE NOT NULL,
    duration_minutes INT NOT NULL,
    intensity ENUM('LOW', 'MEDIUM', 'HIGH', 'VERY_HIGH') NOT NULL,
    session_type ENUM('STRENGTH', 'CONDITIONING', 'SKILLS', 'SCRIMMAGE', 'RECOVERY') NOT NULL,
    recorded_by BINARY(16) NOT NULL,
    notes TEXT,
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_training_load_enrollment FOREIGN KEY (enrollment_id) REFERENCES camp_enrollments (id),
    CONSTRAINT fk_training_load_camp FOREIGN KEY (camp_id) REFERENCES camps (id),
    INDEX idx_training_load_enrollment (enrollment_id),
    INDEX idx_training_load_camp_date (camp_id, session_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Potential predictions
CREATE TABLE IF NOT EXISTS potential_predictions (
    id BINARY(16) NOT NULL,
    athlete_user_id BINARY(16) NOT NULL,
    prediction_tier ENUM('ELITE_PROSPECT', 'HIGH_DEVELOPMENT', 'SOLID_POTENTIAL', 'MONITOR') NOT NULL,
    overall_growth_rate DECIMAL(5,2),
    physical_delta DECIMAL(5,2),
    technical_delta DECIMAL(5,2),
    tactical_delta DECIMAL(5,2),
    projected_score_1yr DECIMAL(5,2),
    projected_score_2yr DECIMAL(5,2),
    confidence_level DECIMAL(5,2),
    calculated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_potential_predictions_user FOREIGN KEY (athlete_user_id) REFERENCES users (id),
    INDEX idx_potential_predictions_athlete (athlete_user_id, calculated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Injury risk assessments
CREATE TABLE IF NOT EXISTS injury_risk_assessments (
    id BINARY(16) NOT NULL,
    athlete_user_id BINARY(16) NOT NULL,
    enrollment_id BINARY(16) NOT NULL,
    risk_score INT NOT NULL,
    risk_level ENUM('LOW', 'MODERATE', 'HIGH', 'CRITICAL') NOT NULL,
    load_factor_score DECIMAL(5,2),
    attendance_factor_score DECIMAL(5,2),
    performance_factor_score DECIMAL(5,2),
    recommendations JSON,
    assessed_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_injury_risk_user FOREIGN KEY (athlete_user_id) REFERENCES users (id),
    CONSTRAINT fk_injury_risk_enrollment FOREIGN KEY (enrollment_id) REFERENCES camp_enrollments (id),
    INDEX idx_injury_risk_enrollment (enrollment_id, assessed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Benchmark profiles
CREATE TABLE IF NOT EXISTS benchmark_profiles (
    id BINARY(16) NOT NULL,
    name VARCHAR(200) NOT NULL,
    benchmark_type ENUM('CAMP', 'STATE', 'COUNTRY', 'REGION', 'HISTORICAL_CHAMPION') NOT NULL,
    position ENUM('QUARTERBACK', 'RUNNING_BACK', 'WIDE_RECEIVER', 'TIGHT_END', 'OFFENSIVE_LINE', 'DEFENSIVE_LINE', 'LINEBACKER', 'CORNERBACK', 'SAFETY', 'KICKER', 'PUNTER', 'RETURNER', 'ATHLETE'),
    age_category ENUM('UNDER_14', 'UNDER_16', 'UNDER_18', 'UNDER_21', 'OPEN'),
    gender ENUM('MALE', 'FEMALE', 'OTHER'),
    physical_benchmark DECIMAL(5,2),
    technical_benchmark DECIMAL(5,2),
    tactical_benchmark DECIMAL(5,2),
    overall_benchmark DECIMAL(5,2),
    source_description TEXT,
    auto_captured BOOLEAN NOT NULL DEFAULT FALSE,
    captured_from_enrollment_id BINARY(16),
    created_by BINARY(16),
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_benchmark_type_position (benchmark_type, position, age_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Benchmark comparisons
CREATE TABLE IF NOT EXISTS benchmark_comparisons (
    id BINARY(16) NOT NULL,
    enrollment_id BINARY(16) NOT NULL,
    benchmark_profile_id BINARY(16) NOT NULL,
    physical_gap DECIMAL(5,2),
    technical_gap DECIMAL(5,2),
    tactical_gap DECIMAL(5,2),
    overall_gap DECIMAL(5,2),
    percentile_rank DECIMAL(5,2),
    compared_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_benchmark_comp_enrollment FOREIGN KEY (enrollment_id) REFERENCES camp_enrollments (id),
    CONSTRAINT fk_benchmark_comp_profile FOREIGN KEY (benchmark_profile_id) REFERENCES benchmark_profiles (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- National ranking configs
CREATE TABLE IF NOT EXISTS national_ranking_configs (
    id BINARY(16) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    weight_formula JSON NOT NULL,
    min_camps_required INT NOT NULL DEFAULT 2,
    age_categories JSON,
    positions JSON,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BINARY(16),
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- National rankings
CREATE TABLE IF NOT EXISTS national_rankings (
    id BINARY(16) NOT NULL,
    athlete_user_id BINARY(16) NOT NULL,
    config_id BINARY(16) NOT NULL,
    composite_score DECIMAL(5,2) NOT NULL,
    rank_value INT NOT NULL,
    total_athletes INT NOT NULL,
    percentile DECIMAL(5,2),
    ranking_scope ENUM('NATIONAL', 'STATE', 'CAMP', 'AGE_GROUP', 'POSITION') NOT NULL,
    scope_value VARCHAR(100),
    position ENUM('QUARTERBACK', 'RUNNING_BACK', 'WIDE_RECEIVER', 'TIGHT_END', 'OFFENSIVE_LINE', 'DEFENSIVE_LINE', 'LINEBACKER', 'CORNERBACK', 'SAFETY', 'KICKER', 'PUNTER', 'RETURNER', 'ATHLETE'),
    age_category ENUM('UNDER_14', 'UNDER_16', 'UNDER_18', 'UNDER_21', 'OPEN'),
    generated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_national_rankings_user FOREIGN KEY (athlete_user_id) REFERENCES users (id),
    CONSTRAINT fk_national_rankings_config FOREIGN KEY (config_id) REFERENCES national_ranking_configs (id),
    INDEX idx_national_rankings_scope (ranking_scope, scope_value, rank_value),
    INDEX idx_national_rankings_athlete (athlete_user_id, generated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Scout profiles
CREATE TABLE IF NOT EXISTS scout_profiles (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    organization VARCHAR(200),
    organization_type ENUM('COLLEGE', 'NFL_ACADEMY', 'PROFESSIONAL_TEAM', 'INDEPENDENT') NOT NULL,
    country VARCHAR(100),
    region VARCHAR(100),
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_scout_profiles_user_id UNIQUE (user_id),
    CONSTRAINT fk_scout_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Scout access scopes
CREATE TABLE IF NOT EXISTS scout_access_scopes (
    id BINARY(16) NOT NULL,
    scout_user_id BINARY(16) NOT NULL,
    scope_type ENUM('COUNTRY', 'STATE', 'CAMP', 'AGE_GROUP', 'POSITION') NOT NULL,
    scope_value VARCHAR(100) NOT NULL,
    granted_by BINARY(16),
    granted_date DATETIME(6) NOT NULL,
    expires_at DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_scout_scopes_user FOREIGN KEY (scout_user_id) REFERENCES users (id),
    INDEX idx_scout_scopes_user (scout_user_id, scope_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Scout bookmarks
CREATE TABLE IF NOT EXISTS scout_bookmarks (
    id BINARY(16) NOT NULL,
    scout_user_id BINARY(16) NOT NULL,
    athlete_user_id BINARY(16) NOT NULL,
    notes TEXT,
    created_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_scout_bookmark UNIQUE (scout_user_id, athlete_user_id),
    CONSTRAINT fk_scout_bookmarks_scout FOREIGN KEY (scout_user_id) REFERENCES users (id),
    CONSTRAINT fk_scout_bookmarks_athlete FOREIGN KEY (athlete_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Interview requests
CREATE TABLE IF NOT EXISTS interview_requests (
    id BINARY(16) NOT NULL,
    scout_user_id BINARY(16) NOT NULL,
    athlete_user_id BINARY(16) NOT NULL,
    preferred_dates JSON,
    notes TEXT,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    reviewed_by BINARY(16),
    review_notes TEXT,
    review_date DATETIME(6),
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_interview_requests_scout FOREIGN KEY (scout_user_id) REFERENCES users (id),
    CONSTRAINT fk_interview_requests_athlete FOREIGN KEY (athlete_user_id) REFERENCES users (id),
    INDEX idx_interview_requests_status (status, created_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Schema Evolution: Ensure ENUM columns include all values
-- (Handles existing databases where CREATE TABLE IF NOT EXISTS is a no-op)
-- =====================================================

-- Add ROLE_SCOUT to users.role ENUM (may already exist on fresh databases)
ALTER TABLE users
    MODIFY COLUMN role ENUM('ROLE_ATHLETE', 'ROLE_COACH', 'ROLE_ADMIN', 'ROLE_SCOUT') NOT NULL;

-- Add ROLE_SCOUT to camp_evaluators.role ENUM (may already exist on fresh databases)
ALTER TABLE camp_evaluators
    MODIFY COLUMN role ENUM('ROLE_COACH', 'ROLE_SCOUT') NOT NULL;
