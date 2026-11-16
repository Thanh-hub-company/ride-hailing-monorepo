-- ========================================================
-- Database: trip_db (trip-service)
-- ========================================================

DROP TABLE IF EXISTS driver_offers CASCADE;
DROP TABLE IF EXISTS trips CASCADE;

CREATE TABLE trips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_code VARCHAR(50) NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    driver_id UUID,
    service_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    pickup_latitude DOUBLE PRECISION NOT NULL,
    pickup_longitude DOUBLE PRECISION NOT NULL,
    dropoff_latitude DOUBLE PRECISION NOT NULL,
    dropoff_longitude DOUBLE PRECISION NOT NULL,
    fare_amount NUMERIC(12,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'ELECTRONIC_WALLET',
    quote_id UUID NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT uk_trips_customer_idempotency UNIQUE (customer_id, idempotency_key)
);

CREATE TABLE driver_offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    driver_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    offered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
    ,CONSTRAINT uk_driver_offer_trip_driver UNIQUE (trip_id, driver_id)
);

CREATE INDEX IF NOT EXISTS idx_trips_customer_id ON trips(customer_id);
CREATE INDEX IF NOT EXISTS idx_trips_driver_id ON trips(driver_id);
CREATE INDEX IF NOT EXISTS idx_trips_status ON trips(status);
CREATE INDEX IF NOT EXISTS idx_offers_trip_id ON driver_offers(trip_id);
CREATE INDEX IF NOT EXISTS idx_offers_driver_id ON driver_offers(driver_id);