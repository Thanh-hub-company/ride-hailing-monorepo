-- ========================================================
-- Database: wallet_db (payment-wallet-service)
-- ========================================================

DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS wallet_entries CASCADE;
DROP TABLE IF EXISTS wallet_ledger CASCADE;
DROP TABLE IF EXISTS wallets CASCADE;

CREATE TABLE wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL UNIQUE,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE wallet_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    trip_id UUID,
    type VARCHAR(50) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    reference_code VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    provider_ref VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_wallets_driver_id ON wallets(driver_id);
CREATE INDEX IF NOT EXISTS idx_entries_wallet_id ON wallet_entries(wallet_id);
CREATE INDEX IF NOT EXISTS idx_entries_trip_id ON wallet_entries(trip_id);
CREATE INDEX IF NOT EXISTS idx_payments_trip_id ON payments(trip_id);
CREATE INDEX IF NOT EXISTS idx_payments_customer_id ON payments(customer_id);

-- Seed wallets for drivers
INSERT INTO wallets (id, driver_id, currency) VALUES
    ('55555555-5555-5555-5555-555555555501', '22222222-2222-2222-2222-222222222201', 'VND'),
    ('55555555-5555-5555-5555-555555555502', '22222222-2222-2222-2222-222222222202', 'VND')
ON CONFLICT (driver_id) DO NOTHING;

-- Seed wallet entries (Initial deposit)
INSERT INTO wallet_entries (id, wallet_id, type, amount, reference_code) VALUES
    ('66666666-6666-6666-6666-666666666601', '55555555-5555-5555-5555-555555555501', 'ADJUSTMENT', 500000.00, 'INITIAL_DEPOSIT'),
    ('66666666-6666-6666-6666-666666666602', '55555555-5555-5555-5555-555555555502', 'ADJUSTMENT', 300000.00, 'INITIAL_DEPOSIT')
ON CONFLICT (id) DO NOTHING;