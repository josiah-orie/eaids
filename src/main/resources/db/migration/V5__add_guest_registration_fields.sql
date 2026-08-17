-- Allow non-authenticated (guest) users to register for events
-- Make user_id nullable for guest registrations
ALTER TABLE event_registrations MODIFY COLUMN user_id BINARY(16) NULL;

-- Add guest fields
ALTER TABLE event_registrations ADD COLUMN guest_name VARCHAR(100) NULL AFTER user_id;
ALTER TABLE event_registrations ADD COLUMN guest_email VARCHAR(255) NULL AFTER guest_name;
ALTER TABLE event_registrations ADD COLUMN guest_phone VARCHAR(20) NULL AFTER guest_email;

-- Drop existing unique constraint (event_id, user_id) since user_id is now nullable
ALTER TABLE event_registrations DROP INDEX uk_event_registration_event_user;

-- Add unique constraint for authenticated users (event_id, user_id) - partial via nullable
-- MySQL handles NULL in unique constraints by allowing multiple NULLs
ALTER TABLE event_registrations ADD CONSTRAINT uk_event_registration_event_user
    UNIQUE (event_id, user_id);

-- Add unique constraint for guest registrations (event_id, guest_email)
ALTER TABLE event_registrations ADD CONSTRAINT uk_event_registration_event_guest_email
    UNIQUE (event_id, guest_email);
