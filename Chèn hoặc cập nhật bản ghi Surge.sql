-- 1. Thêm cột surge_multiplier (mặc định là 1.0)
ALTER TABLE pricing_rules
    ADD COLUMN IF NOT EXISTS surge_multiplier DOUBLE PRECISION DEFAULT 1.0;

-- 2. Cập nhật hệ số surge = 1.5 cho dịch vụ RIDE để test TC 2.2
UPDATE pricing_rules
SET surge_multiplier = 1.5
WHERE service_type = 'RIDE';

-- 3. Kiểm tra lại dữ liệu
SELECT * FROM pricing_rules;