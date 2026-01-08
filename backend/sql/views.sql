-- View: vw_subscription_usage for retrieving subscription usage details per company
CREATE OR REPLACE VIEW vw_subscription_usage AS
SELECT 
    c.id AS company_id,
    c.name AS company_name,
    ll.name AS license_tier,
    ll.max_vehicles,
    ll.max_drivers,
    (SELECT COUNT(*) FROM company_vehicles cv WHERE cv.company_id = c.id) AS current_vehicles,
    (SELECT COUNT(*) FROM company_account ca WHERE ca.company_id = c.id AND 'DRIVER' = ANY(ca.roles)) AS current_drivers
FROM company c
JOIN subscriptions s ON c.id = s.company_id
JOIN license_levels ll ON s.license_id = ll.id
WHERE s.end_date IS NULL OR s.end_date > CURRENT_DATE;

-- View: vw_order_tracking for monitoring active orders with their latest progress
CREATE OR REPLACE VIEW vw_order_tracking AS
SELECT DISTINCT ON (o.id)
    o.id AS order_id,
    -- Joining through company_vehicles to find the owner of the truck on this order
    cv.company_id, 
    o.driver_id,
    o.vehicle_id,
    o.status AS order_status,
    o.pick_up,
    o.delivery,
    p.type AS last_progress_event,
    p.current_pos AS last_known_location,
    p.time AS last_update_time
FROM orders o
JOIN company_vehicles cv ON o.vehicle_id = cv.vehicle_id
LEFT JOIN progress p ON o.id = p.order_id
-- We filter out completed ones
WHERE o.status != 'DELIVERED' AND o.status != 'CANCELED'
ORDER BY o.id, p.time DESC;

-- View: vw_fleet_status for overview of vehicles and their maintenance status per company
CREATE OR REPLACE VIEW vw_fleet_status AS
SELECT 
    cv.company_id,
    v.id AS vehicle_id,
    v.type AS vehicle_type,
    v.load_capacity,
    v.load_type, -- array of normal, refrigerated, hazardous
    v.sensor_data, -- JSONB for real-time temp/fuel
    v.last_odometer,
    -- Get the most recent maintenance record status
    (SELECT status FROM maintenance_records mr 
     WHERE mr.vehicle_id = v.id 
     ORDER BY date_created DESC LIMIT 1) AS current_maintenance_status
FROM vehicles v
JOIN company_vehicles cv ON v.id = cv.vehicle_id;

-- View: vw_user_profiles for retrieving user profiles with roles and preferences
CREATE OR REPLACE VIEW vw_user_profiles AS
SELECT 
    ca.id AS user_id,
    ca.company_id,
    ca.username,
    ca.roles, -- user_role[] enum
    ca.preferences, -- JSONB containing "Warehouse A", "Northern Region", etc.
    ca.first_name,
    ca.last_name
FROM company_account ca;


--View: vw_auth_check for authentication checks including failed login attempts
DROP VIEW IF EXISTS vw_auth_check;

CREATE VIEW vw_auth_check AS
SELECT 
    ca.id AS user_id,
    ca.company_id,
    ca.email,
    ca.username,
    ca.is_active AS is_activated,
    COALESCE(failed_login_counts.failed_attempt_count, 0) AS failed_attempt_count,
    (COALESCE(failed_login_counts.failed_attempt_count, 0) >= 3) AS is_blocked
FROM company_account ca
LEFT JOIN (
    SELECT 
        l.user_id,
        COUNT(*) AS failed_attempt_count
    FROM logins l
    WHERE l.is_successful = FALSE 
      AND l.time > NOW() - INTERVAL '1 hour'
    GROUP BY l.user_id
) AS failed_login_counts ON ca.id = failed_login_counts.user_id;