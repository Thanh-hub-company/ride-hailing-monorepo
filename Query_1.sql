UPDATE driver_profiles
SET status = 'AVAILABLE', is_approved = true
WHERE id IS NOT NULL;

SELECT u.email, dp.status, dp.is_approved
FROM driver_profiles dp
         JOIN users u ON u.id = dp.user_id;