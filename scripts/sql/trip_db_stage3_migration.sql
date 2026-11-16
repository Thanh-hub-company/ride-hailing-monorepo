-- Run this migration once for an existing trip_db before deploying Stage 3.
ALTER TABLE trips ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(255);
UPDATE trips
SET idempotency_key = 'legacy-' || id::text
WHERE idempotency_key IS NULL;
ALTER TABLE trips ALTER COLUMN idempotency_key SET NOT NULL;
ALTER TABLE trips ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
CREATE UNIQUE INDEX IF NOT EXISTS uk_trips_customer_idempotency
    ON trips(customer_id, idempotency_key);
CREATE UNIQUE INDEX IF NOT EXISTS uk_driver_offer_trip_driver
    ON driver_offers(trip_id, driver_id);
