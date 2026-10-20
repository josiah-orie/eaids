-- ============================================================
-- Add ROLE_MEDICAL_OFFICER and ROLE_PHYSIOTHERAPIST to users.role ENUM
-- ============================================================

ALTER TABLE users
    MODIFY COLUMN role ENUM(
        'ROLE_ATHLETE',
        'ROLE_COACH',
        'ROLE_ADMIN',
        'ROLE_SCOUT',
        'ROLE_MEDICAL_OFFICER',
        'ROLE_PHYSIOTHERAPIST'
    ) NOT NULL;
