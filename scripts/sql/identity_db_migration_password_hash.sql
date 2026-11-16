-- Compatibility migration for identity_db schemas created before password_hash.
-- Run this once against the existing identity_db database.

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = current_schema()
          AND table_name = 'users'
          AND column_name = 'password'
    ) THEN
        UPDATE users
        SET password_hash = password
        WHERE password_hash IS NULL
          AND password IS NOT NULL;
    END IF;
END
$$;

-- The shared test accounts use Password123!.
UPDATE users
SET password_hash = '$2a$10$uU/CuS8AZuiaSE0i9pYMkeB6QCM0TAWE9Y.Q4wQOgnWVEOGiTFXIK'
WHERE email IN (
    'customer1@example.com',
    'customer2@example.com',
    'driver1@example.com',
    'driver2@example.com',
    'admin@example.com'
);
