-- Apply this migration to an existing pricing_db without deleting quotes.
ALTER TABLE fare_quotes
    ADD COLUMN IF NOT EXISTS currency VARCHAR(3) NOT NULL DEFAULT 'VND',
    ADD COLUMN IF NOT EXISTS rule_version VARCHAR(50) NOT NULL DEFAULT 'DEFAULT-1';

UPDATE fare_quotes
SET currency = 'VND'
WHERE currency IS NULL;

UPDATE fare_quotes
SET rule_version = 'DEFAULT-1'
WHERE rule_version IS NULL;
