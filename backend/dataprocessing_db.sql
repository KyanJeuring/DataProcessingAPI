-- ENUM DEFINITIONS

DO $$
BEGIN
    CREATE TYPE user_role AS ENUM ('ADMIN', 'MANAGER', 'DRIVER', 'VIEWER');
    EXCEPTION WHEN duplicate_object THEN null;
END
$$;


DO $$
BEGIN
    CREATE TYPE invite_status AS ENUM ('PENDING', 'ACCEPTED', 'EXPIRED');
    EXCEPTION WHEN duplicate_object THEN null;
END
$$;


DO $$
BEGIN
    CREATE TYPE referral_status AS ENUM ('PENDING', 'ACCEPTED', 'EXPIRED');
    EXCEPTION WHEN duplicate_object THEN null;
END
$$;


DO $$
BEGIN
    CREATE TYPE license_name AS ENUM ('BASIC', 'PROFESSIONAL', 'ENTERPRISE');
    EXCEPTION WHEN duplicate_object THEN null;
END
$$;


DO $$
BEGIN
    CREATE TYPE maintenance_status AS ENUM ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED');
    EXCEPTION WHEN duplicate_object THEN null;
END
$$;


DO $$
BEGIN
    CREATE TYPE vehicle_type AS ENUM ('LORRY', 'VAN', 'REFRIGERATED_TRUCK');
    EXCEPTION WHEN duplicate_object THEN null;
END
$$;


DO $$
BEGIN
    CREATE TYPE load_type AS ENUM ('NORMAL', 'REFRIGERATED', 'HAZARDOUS');
    EXCEPTION WHEN duplicate_object THEN null;
END
$$;


DO $$
BEGIN
    CREATE TYPE progress_type AS ENUM (
        'LOADING',
        'DEPARTURE',
        'STOPOVER',
        'BREAK',
        'FUEL',
        'STOP',
        'INSPECTION',
        'DEVIATION',
        'BREAKDOWN',
        'INTERRUPTION',
        'UNLOADING',
        'ARRIVAL',
        'COMPLETION'
    );
    EXCEPTION WHEN duplicate_object THEN null;
END
$$;


-- COMPANIES (parent table for many)

CREATE TABLE IF NOT EXISTS company (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    license BIGINT,
    discount_received BOOLEAN DEFAULT FALSE
);

-- COMPANY_ACCOUNT + AUTH TABLES

CREATE TABLE IF NOT EXISTS company_account (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT REFERENCES companies(id),
    username VARCHAR NOT NULL UNIQUE,
    email VARCHAR NOT NULL UNIQUE,
    password_hash VARCHAR NOT NULL,
    first_name VARCHAR,
    last_name VARCHAR,
    is_active BOOLEAN DEFAULT TRUE,
    date_created TIMESTAMP DEFAULT NOW(),
    roles user_role[] NOT NULL DEFAULT ARRAY[]::user_role[],
    preferences JSONB
);

CREATE TABLE IF NOT EXISTS logins (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES company_account(id),
    time TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS password_recovery (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES company_account(id),
    recovery_token UUID NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS user_invites (
    id BIGSERIAL PRIMARY KEY,
    inviter_id BIGINT REFERENCES company_account(id),
    invitee_email VARCHAR NOT NULL,
    invite_token VARCHAR NOT NULL,
    status invite_status NOT NULL DEFAULT 'pending',
    date_sent TIMESTAMP DEFAULT NOW()
);

-- LICENSE LEVELS + SUBSCRIPTIONS + REFERRALS

CREATE TABLE IF NOT EXISTS license_levels (
    id SERIAL PRIMARY KEY,
    name license_name NOT NULL,
    max_vehicles BIGINT,
    max_drivers BIGINT,
    max_assignments BIGINT,
    monthly_fee DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT NOT NULL REFERENCES company(id),
    license_id BIGINT NOT NULL REFERENCES license_levels(id),
    start_date DATE NOT NULL,
    end_date DATE,
    is_trial BOOLEAN DEFAULT FALSE,
    discount_rate BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS referral_links (
    id BIGSERIAL PRIMARY KEY,
    inviter_company_id BIGINT REFERENCES company(id),
    invited_company_id BIGINT REFERENCES company(id),
    invitation_code VARCHAR NOT NULL,
    date_sent TIMESTAMP DEFAULT NOW(),
    date_accepted TIMESTAMP,
    discount_status referral_status DEFAULT 'pending',
    validity_period_days BIGINT,
    discount_activated_on DATE
);

-- VEHICLES + MAINTENANCE

CREATE TABLE IF NOT EXISTS vehicles (
    id SERIAL PRIMARY KEY,
    load_capacity BIGINT,
    type vehicle_type NOT NULL,
    year_of_manufacture DATE,
    load_type load_type[] NOT NULL DEFAULT ARRAY[]::load_type[],
    maintenance_id BIGINT,
    last_odometer BIGINT,
    sensor_data JSONB
);

CREATE TABLE IF NOT EXISTS maintenance_records (
    id SERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL REFERENCES vehicles(id),
    date DATE,
    odometer BIGINT,
    maintenance_type VARCHAR(255),
    description TEXT,
    cost DECIMAL(10,2),
    status maintenance_status DEFAULT 'scheduled',
    date_created TIMESTAMP DEFAULT NOW(),
    date_modified TIMESTAMP,
    modified_by BIGINT REFERENCES company_account(id)
);

CREATE TABLE IF NOT EXISTS vehicles_maintenance (
    maintenance_id BIGINT REFERENCES maintenance_records(id),
    vehicle_id BIGINT REFERENCES vehicles(id),
    PRIMARY KEY (maintenance_id, vehicle_id)
);

CREATE TABLE IF NOT EXISTS company_vehicles (
    company_id BIGINT REFERENCES company(id),
    vehicle_id BIGINT REFERENCES vehicles(id),
    PRIMARY KEY (company_id, vehicle_id)
);

-- ORDERS + PROGRESS

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    pick_up POINT,
    delivery POINT,
    load_type load_type NOT NULL,
    departure_time TIMESTAMP,
    arrival_time TIMESTAMP,
    status VARCHAR,
    vehicle_id BIGINT REFERENCES vehicles(id),
    driver_id BIGINT REFERENCES company_account(id)
);

CREATE TABLE IF NOT EXISTS progress (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    current_pos POINT,
    time TIMESTAMP DEFAULT NOW(),
    type progress_type NOT NULL,
    description JSONB
);

CREATE TABLE IF NOT EXISTS order_progress (
    order_id BIGINT REFERENCES orders(id),
    progress_id BIGINT REFERENCES progress(id),
    PRIMARY KEY (order_id, progress_id)
);
