-- ============================================================
-- Academy module tables
-- ============================================================

-- Individual applications (public onboarding)
CREATE TABLE IF NOT EXISTS academy_individual_applications (
    id BINARY(16) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    age INT NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    location ENUM('ABUJA', 'LAGOS') NOT NULL,
    addr_country VARCHAR(100),
    addr_state VARCHAR(100),
    addr_lga VARCHAR(100),
    addr_street VARCHAR(255),
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    review_notes TEXT,
    reviewed_by BINARY(16),
    review_date DATETIME(6),
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_academy_individual_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Institution applications (public onboarding)
CREATE TABLE IF NOT EXISTS academy_institution_applications (
    id BINARY(16) NOT NULL,
    institution_type ENUM('PUBLIC_PRIMARY', 'PRIVATE_PRIMARY', 'PUBLIC_SECONDARY', 'PRIVATE_SECONDARY', 'UNIVERSITY', 'POLYTECHNIC', 'COLLEGE_OF_EDUCATION', 'OTHER') NOT NULL,
    name VARCHAR(200) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    addr_country VARCHAR(100),
    addr_state VARCHAR(100),
    addr_lga VARCHAR(100),
    addr_street VARCHAR(255),
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    review_notes TEXT,
    reviewed_by BINARY(16),
    review_date DATETIME(6),
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_academy_institution_contact_email UNIQUE (contact_email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Training schedules
CREATE TABLE IF NOT EXISTS academy_training_schedules (
    id BINARY(16) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    age_category ENUM('UNDER_14', 'UNDER_16', 'UNDER_18', 'UNDER_21', 'OPEN') NOT NULL,
    target_type ENUM('INDIVIDUAL', 'INSTITUTION', 'BOTH') NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    location VARCHAR(200),
    capacity INT NOT NULL,
    status ENUM('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED') NOT NULL DEFAULT 'DRAFT',
    created_date DATETIME(6) NOT NULL,
    last_modified_date DATETIME(6) NOT NULL,
    created_by VARCHAR(255),
    last_modified_by VARCHAR(255),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Schedule staff assignments (coaches/scouts)
CREATE TABLE IF NOT EXISTS academy_schedule_staff (
    id BINARY(16) NOT NULL,
    schedule_id BINARY(16) NOT NULL,
    staff_type ENUM('COACH', 'SCOUT') NOT NULL,
    coach_profile_id BINARY(16),
    scout_profile_id BINARY(16),
    assigned_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_academy_staff_schedule FOREIGN KEY (schedule_id) REFERENCES academy_training_schedules (id),
    CONSTRAINT uk_academy_staff_schedule_coach UNIQUE (schedule_id, coach_profile_id),
    CONSTRAINT uk_academy_staff_schedule_scout UNIQUE (schedule_id, scout_profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Schedule enrollments (approved applicants)
CREATE TABLE IF NOT EXISTS academy_schedule_enrollments (
    id BINARY(16) NOT NULL,
    schedule_id BINARY(16) NOT NULL,
    applicant_type ENUM('INDIVIDUAL', 'INSTITUTION') NOT NULL,
    individual_application_id BINARY(16),
    institution_application_id BINARY(16),
    enrolled_date DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_academy_enrollment_schedule FOREIGN KEY (schedule_id) REFERENCES academy_training_schedules (id),
    CONSTRAINT fk_academy_enrollment_individual FOREIGN KEY (individual_application_id) REFERENCES academy_individual_applications (id),
    CONSTRAINT fk_academy_enrollment_institution FOREIGN KEY (institution_application_id) REFERENCES academy_institution_applications (id),
    CONSTRAINT uk_academy_enrollment_schedule_individual UNIQUE (schedule_id, individual_application_id),
    CONSTRAINT uk_academy_enrollment_schedule_institution UNIQUE (schedule_id, institution_application_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
