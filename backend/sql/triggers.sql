-- Trigger: Enforce max vehicles per company based on license level
CREATE OR REPLACE FUNCTION enforce_vehicle_limit()
RETURNS TRIGGER AS $$
DECLARE
    v_max_vehicles BIGINT;
    v_current_count BIGINT;
BEGIN
    -- Get max vehicles from active subscription
    SELECT ll.max_vehicles INTO v_max_vehicles
    FROM subscriptions s
    JOIN license_levels ll ON s.license_id = ll.id
    WHERE s.company_id = NEW.company_id
      AND (s.end_date IS NULL OR s.end_date > CURRENT_DATE)
    ORDER BY s.start_date DESC
    LIMIT 1;

    IF v_max_vehicles IS NULL THEN
        RAISE EXCEPTION 'No active subscription found for company %', NEW.company_id;
    END IF;

    -- Count current vehicles (after this insert would happen)
    SELECT COUNT(*) INTO v_current_count
    FROM company_vehicles
    WHERE company_id = NEW.company_id;

    IF v_current_count >= v_max_vehicles THEN
        RAISE EXCEPTION 'Vehicle limit exceeded. Company % is limited to % vehicles (license: %)',
            NEW.company_id, v_max_vehicles,
            (SELECT ll.name FROM subscriptions s JOIN license_levels ll ON s.license_id = ll.id 
             WHERE s.company_id = NEW.company_id AND (s.end_date IS NULL OR s.end_date > CURRENT_DATE) 
             ORDER BY s.start_date DESC LIMIT 1);
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_enforce_vehicle_limit
BEFORE INSERT ON company_vehicles
FOR EACH ROW EXECUTE FUNCTION enforce_vehicle_limit();

-- Trigger: Enforce max drivers per company based on license level
CREATE OR REPLACE FUNCTION enforce_driver_limit()
RETURNS TRIGGER AS $$
DECLARE
    v_max_drivers BIGINT;
    v_current_count BIGINT;
    v_company_id BIGINT;
BEGIN
    v_company_id := COALESCE(NEW.company_id, OLD.company_id);

    -- Only check if DRIVER role is being added
    IF (NEW.roles IS NOT NULL) AND ('DRIVER' = ANY(NEW.roles)) AND 
       (OLD.roles IS NULL OR NOT ('DRIVER' = ANY(OLD.roles))) THEN
        
        -- Get max drivers from active subscription
        SELECT ll.max_drivers INTO v_max_drivers
        FROM subscriptions s
        JOIN license_levels ll ON s.license_id = ll.id
        WHERE s.company_id = v_company_id
          AND (s.end_date IS NULL OR s.end_date > CURRENT_DATE)
        ORDER BY s.start_date DESC
        LIMIT 1;

        IF v_max_drivers IS NULL THEN
            RAISE EXCEPTION 'No active subscription found for company %', v_company_id;
        END IF;

        -- Count current active drivers
        SELECT COUNT(*) INTO v_current_count
        FROM company_account
        WHERE company_id = v_company_id
          AND is_active = TRUE
          AND 'DRIVER' = ANY(roles);

        IF v_current_count >= v_max_drivers THEN
            RAISE EXCEPTION 'Driver limit exceeded. Company % is limited to % drivers (license: %)',
                v_company_id, v_max_drivers,
                (SELECT ll.name FROM subscriptions s JOIN license_levels ll ON s.license_id = ll.id 
                 WHERE s.company_id = v_company_id AND (s.end_date IS NULL OR s.end_date > CURRENT_DATE) 
                 ORDER BY s.start_date DESC LIMIT 1);
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_enforce_driver_limit
BEFORE INSERT OR UPDATE OF roles ON company_account
FOR EACH ROW EXECUTE FUNCTION enforce_driver_limit();
