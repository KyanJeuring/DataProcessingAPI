
--- Function: sp_read_auth_check(p_email VARCHAR)
CREATE OR REPLACE FUNCTION sp_read_auth_check(p_email VARCHAR)
RETURNS SETOF vw_auth_check AS $$
BEGIN
    RETURN QUERY 
    SELECT * FROM vw_auth_check 
    WHERE email = p_email;
END;
$$ LANGUAGE plpgsql;

-- Function: sp_register_company(p_company_name VARCHAR, p_license license_name DEFAULT 'BASIC', p_is_trial BOOLEAN DEFAULT TRUE)
-- Purpose: Registers a new company and creates an initial subscription (90-day trial by default)
-- Returns created company id, subscription id, dates, and license id
CREATE OR REPLACE FUNCTION sp_register_company(
    p_company_name VARCHAR,
    p_license license_name DEFAULT 'BASIC',
    p_is_trial BOOLEAN DEFAULT TRUE
) RETURNS TABLE (
    company_id BIGINT,
    subscription_id BIGINT,
    start_date DATE,
    end_date DATE,
    license_id BIGINT
) AS $$
DECLARE
    v_company_id BIGINT;
    v_license_id BIGINT;
    v_subscription_id BIGINT;
    v_end_date DATE;
BEGIN
    -- Prevent duplicate company names (case-insensitive)
    IF EXISTS (SELECT 1 FROM company WHERE LOWER(name) = LOWER(p_company_name)) THEN
        RAISE EXCEPTION 'Company "%" already exists', p_company_name;
    END IF;

    -- Resolve license level id from enum name
    SELECT id INTO v_license_id FROM license_levels WHERE name = p_license;
    IF v_license_id IS NULL THEN
        RAISE EXCEPTION 'License level % not found', p_license;
    END IF;

    -- Create company and set its license field for convenience
    INSERT INTO company(name, license)
    VALUES (p_company_name, v_license_id)
    RETURNING id INTO v_company_id;

    -- Determine trial end date (90 days) or NULL for non-trial
    v_end_date := CASE WHEN p_is_trial THEN (CURRENT_DATE + INTERVAL '90 days')::DATE ELSE NULL END;

    -- Create initial subscription
    INSERT INTO subscriptions(company_id, license_id, start_date, end_date, is_trial, discount_rate)
    VALUES (v_company_id, v_license_id, CURRENT_DATE, v_end_date, p_is_trial, 0)
    RETURNING id INTO v_subscription_id;

    RETURN QUERY SELECT v_company_id, v_subscription_id, CURRENT_DATE::DATE, v_end_date, v_license_id;
END;
$$ LANGUAGE plpgsql;

--- Function: sp_read_fleet_status(p_company_id BIGINT)
CREATE OR REPLACE FUNCTION sp_read_fleet_status(p_company_id BIGINT)
RETURNS SETOF vw_fleet_status AS $$
BEGIN
    RETURN QUERY 
    SELECT * FROM vw_fleet_status 
    WHERE company_id = p_company_id;
END;
$$ LANGUAGE plpgsql;

--- Function: sp_read_order_tracking(p_company_id BIGINT)
CREATE OR REPLACE FUNCTION sp_read_order_tracking(p_company_id BIGINT)
RETURNS SETOF vw_order_tracking AS $$
BEGIN
    RETURN QUERY 
    SELECT * FROM vw_order_tracking 
    WHERE company_id = p_company_id;
END;
$$ LANGUAGE plpgsql;

--- Function: sp_read_subscription_usage(p_company_id BIGINT)
CREATE OR REPLACE FUNCTION sp_read_subscription_usage(p_company_id BIGINT)
RETURNS SETOF vw_subscription_usage AS $$
BEGIN
    RETURN QUERY 
    SELECT * FROM vw_subscription_usage 
    WHERE company_id = p_company_id;
END;
$$ LANGUAGE plpgsql;

-- Function: sp_register_user(p_company_id BIGINT, p_username VARCHAR, p_email VARCHAR, p_password_hash VARCHAR)
-- Purpose: Registers a new user account for an existing company, generates activation token
-- Notes:
--  - User is created with is_active = FALSE (must activate via token)
--  - Activation token is stored in user_invites table
--  - Token expires after 24 hours
--  - Returns user_id and activation_token to send via email
CREATE OR REPLACE FUNCTION sp_register_user(
    p_company_id BIGINT,
    p_username VARCHAR,
    p_email VARCHAR,
    p_password_hash VARCHAR,
    p_first_name VARCHAR DEFAULT NULL,
    p_last_name VARCHAR DEFAULT NULL
) RETURNS TABLE (
    user_id BIGINT,
    activation_token VARCHAR,
    expires_at TIMESTAMP
) AS $$
DECLARE
    v_user_id BIGINT;
    v_token VARCHAR := REPLACE(gen_random_uuid()::text, '-', '');
    v_expiry TIMESTAMP := NOW() + INTERVAL '24 hours';
BEGIN
    -- Verify company exists
    IF NOT EXISTS (SELECT 1 FROM company WHERE id = p_company_id) THEN
        RAISE EXCEPTION 'Company with id % does not exist', p_company_id;
    END IF;

    -- Prevent duplicate email or username (case-insensitive)
    IF EXISTS (SELECT 1 FROM company_account WHERE LOWER(email) = LOWER(p_email)) THEN
        RAISE EXCEPTION 'Email address % is already registered', p_email;
    END IF;
    
    IF EXISTS (SELECT 1 FROM company_account WHERE LOWER(username) = LOWER(p_username)) THEN
        RAISE EXCEPTION 'Username % is already taken', p_username;
    END IF;

    -- Create user account with is_active = FALSE (needs activation)
    INSERT INTO company_account(
        company_id, username, email, password_hash, 
        first_name, last_name, is_active, roles, preferences
    )
    VALUES (
        p_company_id, p_username, p_email, p_password_hash,
        p_first_name, p_last_name, FALSE, ARRAY[]::user_role[], '{}'::jsonb
    )
    RETURNING id INTO v_user_id;

    -- Store activation token in user_invites (inviter_id is NULL for self-registration)
    INSERT INTO user_invites(inviter_id, invitee_email, invite_token, status, date_sent)
    VALUES (NULL, p_email, v_token, 'PENDING', NOW());

    RETURN QUERY SELECT v_user_id, v_token, v_expiry;
END;
$$ LANGUAGE plpgsql;

-- Function: sp_activate_user(p_token VARCHAR)
-- Purpose: Activates a user account using the verification token
-- Notes:
--  - Validates token exists and is still PENDING
--  - Sets user account is_active = TRUE
--  - Marks token as ACCEPTED to prevent reuse
CREATE OR REPLACE FUNCTION sp_activate_user(
    p_token VARCHAR
) RETURNS TABLE (
    user_id BIGINT,
    company_id BIGINT,
    email VARCHAR,
    activated BOOLEAN
) AS $$
DECLARE
    v_invite_id BIGINT;
    v_email VARCHAR;
    v_user_id BIGINT;
    v_company_id BIGINT;
BEGIN
    -- Find the pending activation token
    SELECT ui.id, ui.invitee_email
    INTO v_invite_id, v_email
    FROM user_invites ui
    WHERE ui.invite_token = p_token
      AND ui.status = 'PENDING'
      AND ui.inviter_id IS NULL  -- Only self-registration activations
    ORDER BY ui.date_sent DESC
    LIMIT 1;

    IF v_invite_id IS NULL THEN
        RAISE EXCEPTION 'Invalid or expired activation token';
    END IF;

    -- Find the user account by email
    SELECT id, company_id INTO v_user_id, v_company_id
    FROM company_account
    WHERE LOWER(email) = LOWER(v_email);

    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'No user account found for email %', v_email;
    END IF;

    -- Activate the user account
    UPDATE company_account SET is_active = TRUE WHERE id = v_user_id;

    -- Mark token as accepted to prevent reuse
    UPDATE user_invites SET status = 'ACCEPTED' WHERE id = v_invite_id;

    RETURN QUERY SELECT v_user_id, v_company_id, v_email, TRUE;
END;
$$ LANGUAGE plpgsql;

-- Function: sp_request_password_recovery(p_email VARCHAR)
-- Purpose: Generates a password recovery token for a user
-- Notes:
--  - Only works for active user accounts
--  - Recovery token expires after 1 hour
--  - Stores token in password_recovery table with UUID type
--  - Returns token to send via email
CREATE OR REPLACE FUNCTION sp_request_password_recovery(
    p_email VARCHAR
) RETURNS TABLE (
    user_id BIGINT,
    recovery_token UUID,
    expires_at TIMESTAMP
) AS $$
DECLARE
    v_user_id BIGINT;
    v_token UUID := gen_random_uuid();
    v_expiry TIMESTAMP := NOW() + INTERVAL '1 hour';
BEGIN
    -- Find active user by email
    SELECT id INTO v_user_id
    FROM company_account
    WHERE LOWER(email) = LOWER(p_email)
      AND is_active = TRUE;

    IF v_user_id IS NULL THEN
        RAISE EXCEPTION 'No active user found with email %', p_email;
    END IF;

    -- Deactivate any existing recovery tokens for this user
    UPDATE password_recovery 
    SET is_active = FALSE 
    WHERE user_id = v_user_id AND is_active = TRUE;

    -- Create new recovery token
    INSERT INTO password_recovery(user_id, recovery_token, expiry_time, is_active)
    VALUES (v_user_id, v_token, v_expiry, TRUE);

    RETURN QUERY SELECT v_user_id, v_token, v_expiry;
END;
$$ LANGUAGE plpgsql;

-- Function: sp_reset_password(p_token UUID, p_new_password_hash VARCHAR)
-- Purpose: Resets user password using valid recovery token
-- Notes:
--  - Validates token is active and not expired
--  - Updates password_hash in company_account
--  - Deactivates recovery token to prevent reuse
CREATE OR REPLACE FUNCTION sp_reset_password(
    p_token UUID,
    p_new_password_hash VARCHAR
) RETURNS TABLE (
    user_id BIGINT,
    email VARCHAR,
    password_reset BOOLEAN
) AS $$
DECLARE
    v_recovery RECORD;
    v_email VARCHAR;
BEGIN
    -- Find valid recovery token
    SELECT pr.user_id, pr.id
    INTO v_recovery
    FROM password_recovery pr
    WHERE pr.recovery_token = p_token
      AND pr.is_active = TRUE
      AND pr.expiry_time > NOW();

    IF v_recovery IS NULL THEN
        RAISE EXCEPTION 'Invalid or expired recovery token';
    END IF;

    -- Update user password
    UPDATE company_account 
    SET password_hash = p_new_password_hash
    WHERE id = v_recovery.user_id
    RETURNING email INTO v_email;

    -- Deactivate recovery token
    UPDATE password_recovery 
    SET is_active = FALSE 
    WHERE recovery_token = p_token;

    RETURN QUERY SELECT v_recovery.user_id, v_email, TRUE;
END;
$$ LANGUAGE plpgsql;

-- Function: sp_attempt_login(p_identity VARCHAR, p_password_hash VARCHAR)
-- Purpose: Attempts to log in a user with email/username and password
-- Notes:
--  - Accepts either email or username as identity
--  - Checks if account is active and not blocked (via vw_auth_check)
--  - Records login attempt in logins table (successful or failed)
--  - Returns user info, success status, and block status
--  - After 3 failed attempts in last hour, account is blocked
CREATE OR REPLACE FUNCTION sp_attempt_login(
    p_identity VARCHAR,
    p_password_hash VARCHAR
) RETURNS TABLE (
    user_id BIGINT,
    company_id BIGINT,
    username VARCHAR,
    email VARCHAR,
    is_successful BOOLEAN,
    is_blocked BOOLEAN,
    failed_attempts BIGINT,
    reason VARCHAR
) AS $$
DECLARE
    v_user RECORD;
    v_auth RECORD;
    v_success BOOLEAN := FALSE;
    v_reason VARCHAR := NULL;
BEGIN
    -- Find user by email or username (case-insensitive)
    SELECT ca.id, ca.company_id, ca.username, ca.email, ca.password_hash, ca.is_active
    INTO v_user
    FROM company_account ca
    WHERE LOWER(ca.email) = LOWER(p_identity) 
       OR LOWER(ca.username) = LOWER(p_identity);

    -- User not found
    IF v_user IS NULL THEN
        RETURN QUERY SELECT 
            NULL::BIGINT, NULL::BIGINT, NULL::VARCHAR, NULL::VARCHAR,
            FALSE, FALSE, 0::BIGINT, 'USER_NOT_FOUND';
        RETURN;
    END IF;

    -- Get auth check info (activation status, failed attempts, block status)
    SELECT * INTO v_auth 
    FROM vw_auth_check 
    WHERE vw_auth_check.user_id = v_user.id;

    -- Check if account is not activated
    IF NOT v_user.is_active THEN
        INSERT INTO logins(user_id, is_successful) VALUES (v_user.id, FALSE);
        RETURN QUERY SELECT 
            v_user.id, v_user.company_id, v_user.username, v_user.email,
            FALSE, COALESCE(v_auth.is_blocked, FALSE), COALESCE(v_auth.failed_attempt_count, 0),
            'NOT_ACTIVATED';
        RETURN;
    END IF;

    -- Check if account is blocked (3+ failed attempts in last hour)
    IF v_auth.is_blocked THEN
        INSERT INTO logins(user_id, is_successful) VALUES (v_user.id, FALSE);
        RETURN QUERY SELECT 
            v_user.id, v_user.company_id, v_user.username, v_user.email,
            FALSE, TRUE, v_auth.failed_attempt_count, 'ACCOUNT_BLOCKED';
        RETURN;
    END IF;

    -- Verify password
    IF v_user.password_hash = p_password_hash THEN
        v_success := TRUE;
        v_reason := NULL;
    ELSE
        v_success := FALSE;
        v_reason := 'INVALID_PASSWORD';
    END IF;

    -- Record login attempt
    INSERT INTO logins(user_id, is_successful) VALUES (v_user.id, v_success);

    -- Re-check block status after this attempt
    SELECT * INTO v_auth 
    FROM vw_auth_check 
    WHERE vw_auth_check.user_id = v_user.id;

    RETURN QUERY SELECT 
        v_user.id, v_user.company_id, v_user.username, v_user.email,
        v_success, COALESCE(v_auth.is_blocked, FALSE), COALESCE(v_auth.failed_attempt_count, 0),
        v_reason;
END;
$$ LANGUAGE plpgsql;

-- Function: sp_assign_role(p_user_id BIGINT, p_role user_role)
-- Purpose: Assigns an operational role to a user
-- Notes:
--  - Roles array represents operational profiles (ADMIN, MANAGER, DRIVER, VIEWER)
--  - A user can have multiple roles for different operational contexts
--  - Location and work preferences stored in preferences JSONB
CREATE OR REPLACE FUNCTION sp_assign_role(
    p_user_id BIGINT,
    p_role user_role
) RETURNS BOOLEAN AS $$
DECLARE
    v_roles user_role[];
BEGIN
    SELECT roles INTO v_roles FROM company_account WHERE id = p_user_id;
    
    IF NOT FOUND THEN
        RETURN FALSE;
    END IF;

    IF v_roles IS NULL THEN
        v_roles := ARRAY[]::user_role[];
    END IF;

    -- Add role if not already present
    IF NOT (p_role = ANY(v_roles)) THEN
        v_roles := array_append(v_roles, p_role);
        UPDATE company_account SET roles = v_roles WHERE id = p_user_id;
    END IF;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- Function: sp_remove_role(p_user_id BIGINT, p_role user_role)
-- Purpose: Removes an operational role from a user
CREATE OR REPLACE FUNCTION sp_remove_role(
    p_user_id BIGINT,
    p_role user_role
) RETURNS BOOLEAN AS $$
BEGIN
    UPDATE company_account 
    SET roles = array_remove(roles, p_role)
    WHERE id = p_user_id;
    
    RETURN FOUND;
END;
$$ LANGUAGE plpgsql;

-- Function: sp_update_work_preferences(p_user_id BIGINT, p_location VARCHAR, p_work_preferences JSONB)
-- Purpose: Updates user's location and work preferences
-- Notes:
--  - location: e.g., "Warehouse A", "Northern Region"
--  - work_preferences: filters like vehicle capacity, load types, route preferences
CREATE OR REPLACE FUNCTION sp_update_work_preferences(
    p_user_id BIGINT,
    p_location VARCHAR DEFAULT NULL,
    p_work_preferences JSONB DEFAULT NULL
) RETURNS BOOLEAN AS $$
DECLARE
    v_prefs JSONB;
BEGIN
    SELECT COALESCE(preferences, '{}'::jsonb) INTO v_prefs
    FROM company_account WHERE id = p_user_id;

    IF p_location IS NOT NULL THEN
        v_prefs := jsonb_set(v_prefs, '{location}', to_jsonb(p_location));
    END IF;

    IF p_work_preferences IS NOT NULL THEN
        v_prefs := jsonb_set(v_prefs, '{work_preferences}', p_work_preferences);
    END IF;

    UPDATE company_account SET preferences = v_prefs WHERE id = p_user_id;
    
    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;

-- Function: sp_create_order(p_vehicle_id BIGINT, p_driver_id BIGINT, ...)
-- Purpose: Creates a new transport order
CREATE OR REPLACE FUNCTION sp_create_order(
    p_vehicle_id BIGINT,
    p_driver_id BIGINT,
    p_pick_up POINT,
    p_delivery POINT,
    p_load_type load_type,
    p_departure_time TIMESTAMP DEFAULT NULL,
    p_arrival_time TIMESTAMP DEFAULT NULL
) RETURNS BIGINT AS $$
DECLARE
    v_order_id BIGINT;
BEGIN
    -- Verify vehicle and driver exist
    IF NOT EXISTS (SELECT 1 FROM vehicles WHERE id = p_vehicle_id) THEN
        RAISE EXCEPTION 'Vehicle % does not exist', p_vehicle_id;
    END IF;
    
    IF NOT EXISTS (SELECT 1 FROM company_account WHERE id = p_driver_id) THEN
        RAISE EXCEPTION 'Driver % does not exist', p_driver_id;
    END IF;

    INSERT INTO orders(pick_up, delivery, load_type, departure_time, arrival_time, status, vehicle_id, driver_id)
    VALUES (p_pick_up, p_delivery, p_load_type, p_departure_time, p_arrival_time, 'PENDING', p_vehicle_id, p_driver_id)
    RETURNING id INTO v_order_id;

    RETURN v_order_id;
END;
$$ LANGUAGE plpgsql;

-- Function: sp_add_progress(p_order_id BIGINT, p_position POINT, p_type progress_type, p_description JSONB)
-- Purpose: Adds a progress entry to a transport order
CREATE OR REPLACE FUNCTION sp_add_progress(
    p_order_id BIGINT,
    p_position POINT,
    p_type progress_type,
    p_description JSONB DEFAULT '{}'
) RETURNS BIGINT AS $$
DECLARE
    v_progress_id BIGINT;
BEGIN
    IF NOT EXISTS (SELECT 1 FROM orders WHERE id = p_order_id) THEN
        RAISE EXCEPTION 'Order % does not exist', p_order_id;
    END IF;

    INSERT INTO progress(order_id, current_pos, type, description)
    VALUES (p_order_id, p_position, p_type, p_description)
    RETURNING id INTO v_progress_id;

    -- Update order status based on progress type
    IF p_type = 'DEPARTURE' THEN
        UPDATE orders SET status = 'IN_TRANSIT' WHERE id = p_order_id;
    ELSIF p_type IN ('ARRIVAL', 'COMPLETION') THEN
        UPDATE orders SET status = 'DELIVERED' WHERE id = p_order_id;
    END IF;

    RETURN v_progress_id;
END;
$$ LANGUAGE plpgsql;

-- Function: sp_activate_referral_discount(p_inviter_company_id BIGINT, p_invited_company_id BIGINT)
-- Purpose: Activates referral discount for both companies once invited company has paid license
CREATE OR REPLACE FUNCTION sp_activate_referral_discount(
    p_inviter_company_id BIGINT,
    p_invited_company_id BIGINT,
    p_discount_rate BIGINT DEFAULT 10,
    p_validity_days BIGINT DEFAULT 90
) RETURNS BOOLEAN AS $$
DECLARE
    v_link RECORD;
BEGIN
    -- Find referral link
    SELECT * INTO v_link
    FROM referral_links
    WHERE inviter_company_id = p_inviter_company_id
      AND invited_company_id = p_invited_company_id
      AND discount_status = 'ACCEPTED';

    IF v_link IS NULL THEN
        RAISE EXCEPTION 'No accepted referral link found between companies % and %', 
            p_inviter_company_id, p_invited_company_id;
    END IF;

    -- Check both companies haven't already received discount
    IF (SELECT discount_received FROM company WHERE id = p_inviter_company_id) THEN
        RAISE EXCEPTION 'Inviter company % has already received a referral discount', p_inviter_company_id;
    END IF;
    
    IF (SELECT discount_received FROM company WHERE id = p_invited_company_id) THEN
        RAISE EXCEPTION 'Invited company % has already received a referral discount', p_invited_company_id;
    END IF;

    -- Apply discount to both companies' active subscriptions
    UPDATE subscriptions
    SET discount_rate = p_discount_rate
    WHERE company_id IN (p_inviter_company_id, p_invited_company_id)
      AND (end_date IS NULL OR end_date > CURRENT_DATE);

    -- Mark companies as having received discount
    UPDATE company SET discount_received = TRUE 
    WHERE id IN (p_inviter_company_id, p_invited_company_id);

    -- Update referral link
    UPDATE referral_links
    SET discount_activated_on = CURRENT_DATE,
        validity_period_days = p_validity_days
    WHERE id = v_link.id;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;
