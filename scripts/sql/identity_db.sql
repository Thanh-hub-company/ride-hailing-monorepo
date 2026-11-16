-- ========================================================
-- Database: identity_db (identity-service)
-- ========================================================

DROP TABLE IF EXISTS driver_profiles CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE driver_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    vehicle_license_plate VARCHAR(50) NOT NULL,
    vehicle_model VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    rating DOUBLE PRECISION NOT NULL DEFAULT 5.0,
    is_approved BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(1000) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);

-- INDEXES
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone_number);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);

-- SEED DATA (CUSTOMER, DRIVER & ADMIN) - Password: Password123!
INSERT INTO users (id, email, phone_number, password_hash, full_name, role) VALUES
    ('11111111-1111-1111-1111-111111111101', 'customer1@example.com', '0901234567', '$2a$10$Y7f8tqDsWQ22ya6bAn42MONFXKPZROXk0VKf6YL1dh2AIfoWrwfyC', 'Nguyen Van Khach', 'CUSTOMER'),
    ('11111111-1111-1111-1111-111111111102', 'customer2@example.com', '0902222222', '$2a$10$Y7f8tqDsWQ22ya6bAn42MONFXKPZROXk0VKf6YL1dh2AIfoWrwfyC', 'Tran Thi B', 'CUSTOMER'),
    ('22222222-2222-2222-2222-222222222201', 'driver1@example.com', '0987654321', '$2a$10$Y7f8tqDsWQ22ya6bAn42MONFXKPZROXk0VKf6YL1dh2AIfoWrwfyC', 'Tran Van Taixe', 'DRIVER'),
    ('22222222-2222-2222-2222-222222222202', 'driver2@example.com', '0988888888', '$2a$10$Y7f8tqDsWQ22ya6bAn42MONFXKPZROXk0VKf6YL1dh2AIfoWrwfyC', 'Le Van Tai', 'DRIVER'),
    ('33333333-3333-3333-3333-333333333301', 'admin@example.com', '0999999999', '$2a$10$Y7f8tqDsWQ22ya6bAn42MONFXKPZROXk0VKf6YL1dh2AIfoWrwfyC', 'System Admin', 'ADMIN')
ON CONFLICT (id) DO NOTHING;

INSERT INTO driver_profiles (id, user_id, vehicle_license_plate, vehicle_model, status, rating, is_approved) VALUES
    ('22222222-2222-2222-2222-222222222211', '22222222-2222-2222-2222-222222222201', '51A-123.45', 'Honda Wave Alpha', 'OFFLINE', 5.0, TRUE),
    ('22222222-2222-2222-2222-222222222212', '22222222-2222-2222-2222-222222222202', '51B-678.90', 'Toyota Vios', 'OFFLINE', 4.8, TRUE)
ON CONFLICT (id) DO NOTHING;