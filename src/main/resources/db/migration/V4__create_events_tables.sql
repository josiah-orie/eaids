-- Events table
CREATE TABLE IF NOT EXISTS events (
    id BINARY(16) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    event_type VARCHAR(100) NOT NULL,
    status ENUM('DRAFT', 'REGISTRATION_OPEN', 'REGISTRATION_CLOSED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT',
    location VARCHAR(200) NOT NULL,
    country VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    start_time TIME,
    end_time TIME,
    registration_deadline DATE,
    capacity INT NOT NULL,
    current_registration_count INT NOT NULL DEFAULT 0,
    event_image VARCHAR(512),
    image_path VARCHAR(512),
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Event registrations table
CREATE TABLE IF NOT EXISTS event_registrations (
    id BINARY(16) NOT NULL,
    event_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    status ENUM('REGISTERED', 'CONFIRMED', 'ATTENDED', 'NO_SHOW', 'CANCELLED') NOT NULL DEFAULT 'REGISTERED',
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_event_registration_event_user UNIQUE (event_id, user_id),
    CONSTRAINT fk_event_registrations_event FOREIGN KEY (event_id) REFERENCES events (id),
    CONSTRAINT fk_event_registrations_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
