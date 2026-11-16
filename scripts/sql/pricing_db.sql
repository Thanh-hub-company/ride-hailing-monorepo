-- ========================================================
-- Database: pricing_db (pricing-service)
-- ========================================================

DROP TABLE IF EXISTS fare_quotes CASCADE;
DROP TABLE IF EXISTS pricing_rules CASCADE;

CREATE TABLE pricing_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    service_type VARCHAR(50) NOT NULL UNIQUE,
    base_fare NUMERIC(12,2) NOT NULL,
    per_km_rate NUMERIC(12,2) NOT NULL,
    per_minute_rate NUMERIC(12,2) NOT NULL,
    minimum_fare NUMERIC(12,2) NOT NULL,
    surge_multiplier DOUBLE PRECISION NOT NULL DEFAULT 1.0
);

CREATE TABLE fare_quotes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID,
    service_type VARCHAR(50) NOT NULL,
    pickup_latitude DOUBLE PRECISION NOT NULL,
    pickup_longitude DOUBLE PRECISION NOT NULL,
    dropoff_latitude DOUBLE PRECISION NOT NULL,
    dropoff_longitude DOUBLE PRECISION NOT NULL,
    distance_km DOUBLE PRECISION NOT NULL,
    estimated_duration_minutes DOUBLE PRECISION NOT NULL,
    fare_amount NUMERIC(12,2) NOT NULL,
    surge_multiplier DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    rule_version VARCHAR(50) NOT NULL DEFAULT 'DEFAULT-1'
);

ALTER TABLE fare_quotes
    ADD COLUMN IF NOT EXISTS currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    ADD COLUMN IF NOT EXISTS rule_version VARCHAR(50) NOT NULL DEFAULT 'DEFAULT-1';

INSERT INTO pricing_rules (id, service_type, base_fare, per_km_rate, per_minute_rate, minimum_fare, surge_multiplier) VALUES
    ('44444444-4444-4444-4444-444444444401', 'RIDE', 15000.00, 10000.00, 800.00, 20000.00, 1.0),
    ('44444444-4444-4444-4444-444444444402', 'DELIVERY', 12000.00, 8000.00, 500.00, 15000.00, 1.0)
ON CONFLICT (service_type) DO UPDATE SET
    base_fare = EXCLUDED.base_fare,
    per_km_rate = EXCLUDED.per_km_rate,
    per_minute_rate = EXCLUDED.per_minute_rate,
    minimum_fare = EXCLUDED.minimum_fare,
    surge_multiplier = EXCLUDED.surge_multiplier;