-- ENUM DEFINITIONS

DO $$
BEGIN
    CREATE TYPE user_role AS ENUM ('admin', 'manager', 'driver', 'viewer');
    EXCEPTION WHEN duplicate_object THEN null;
END
$$;


DO $$
BEGIN
    CREATE TYPE invite_status AS ENUM ('pending', 'accepted', 'expired');
    EXCEPTION WHEN duplicate_object THEN null;
END
$$;


DO $$
BEGIN
    CREATE TYPE referral_status AS ENUM ('pending', 'accepted', 'expired');
    EXCEPTION WHEN duplicate_object THEN null;
END
$$;


DO $$
BEGIN
    CREATE TYPE license_name AS ENUM ('Basic', 'Professional', 'Enterprise');
    EXCEPTION WHEN duplicate_object THEN null;
END
$$;


DO $$
BEGIN
    CREATE TYPE maintenance_status AS ENUM ('scheduled', 'in_progress', 'completed', 'cancelled');
    EXCEPTION WHEN duplicate_object THEN null;
END
$$;


DO $$
BEGIN
    CREATE TYPE vehicle_type AS ENUM ('lorry', 'van', 'refrigerated_truck');
    EXCEPTION WHEN duplicate_object THEN null;
END
$$;


DO $$
BEGIN
    CREATE TYPE load_type AS ENUM ('normal', 'refrigerated', 'hazardous');
    EXCEPTION WHEN duplicate_object THEN null;
END
$$;


DO $$
BEGIN
    CREATE TYPE progress_type AS ENUM (
        'loading',
        'departure',
        'stopover',
        'break',
        'fuel',
        'stop',
        'inspection',
        'deviation',
        'breakdown',
        'interruption',
        'unloading',
        'arrival',
        'completion'
    );
    EXCEPTION WHEN duplicate_object THEN null;
END
$$;


-- COMPANIES (parent table for many)

CREATE TABLE IF NOT EXISTS companies (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR NOT NULL,
    license BIGINT,
    discountRecieved BOOLEAN DEFAULT FALSE
);

-- COMPANY_ACCOUNT + AUTH TABLES

CREATE TABLE IF NOT EXISTS company_account (
    id BIGSERIAL PRIMARY KEY,
    company_id BIGINT REFERENCES companies(id),
    username VARCHAR NOT NULL UNIQUE,
    email VARCHAR NOT NULL UNIQUE,
    passwordHash VARCHAR NOT NULL,
    firstName VARCHAR,
    lastName VARCHAR,
    isActive BOOLEAN DEFAULT TRUE,
    dateCreated TIMESTAMP DEFAULT NOW(),
    roles user_role[] NOT NULL DEFAULT ARRAY[]::user_role[],
    preferences JSONB
);

CREATE TABLE IF NOT EXISTS logins (
    id BIGSERIAL PRIMARY KEY,
    userId BIGINT NOT NULL REFERENCES company_account(id),
    time TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS password_recovery (
    id BIGSERIAL PRIMARY KEY,
    userId BIGINT NOT NULL REFERENCES company_account(id),
    recoveryToken UUID NOT NULL,
    expiryTime TIMESTAMP NOT NULL,
    isActive BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS user_invites (
    id BIGSERIAL PRIMARY KEY,
    inviter_id BIGINT REFERENCES company_account(id),
    inviteeEmail VARCHAR NOT NULL,
    inviteToke VARCHAR NOT NULL,
    status invite_status NOT NULL DEFAULT 'pending',
    dateSent TIMESTAMP DEFAULT NOW()
);

-- LICENSE LEVELS + SUBSCRIPTIONS + REFERRALS

CREATE TABLE IF NOT EXISTS license_levels (
    id SERIAL PRIMARY KEY,
    name license_name NOT NULL,
    MAX_VEHICLES BIGINT,
    MAX_DRIVERS BIGINT,
    MAX_ASSIGNMENTS BIGINT,
    monthlyFee DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    companyId BIGINT NOT NULL REFERENCES companies(id),
    licenseId BIGINT NOT NULL REFERENCES license_levels(id),
    startDate DATE NOT NULL,
    endDate DATE,
    isTrial BOOLEAN DEFAULT FALSE,
    discountRate BIGINT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS referral_links (
    id BIGSERIAL PRIMARY KEY,
    inviterCompanyId BIGINT REFERENCES companies(id),
    invitedCompanyId BIGINT REFERENCES companies(id),
    invitationCode VARCHAR NOT NULL,
    dateSent TIMESTAMP DEFAULT NOW(),
    dateAccepted TIMESTAMP,
    discountStatus referral_status DEFAULT 'pending',
    validityPeriodDays BIGINT,
    discountActivatedOn DATE
);

-- VEHICLES + MAINTENANCE

CREATE TABLE IF NOT EXISTS vehicles (
    id SERIAL PRIMARY KEY,
    loadCapacity BIGINT,
    type vehicle_type NOT NULL,
    yearOfManufacture DATE,
    loadType load_type[] NOT NULL DEFAULT ARRAY[]::load_type[],
    maintenanceId BIGINT,
    lastOdometer BIGINT,
    sensorData JSONB
);

CREATE TABLE IF NOT EXISTS maintenance_records (
    id SERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL REFERENCES vehicles(id),
    date DATE,
    odometer BIGINT,
    maintenanceType VARCHAR(255),
    description TEXT,
    cost DECIMAL(10,2),
    status maintenance_status DEFAULT 'scheduled',
    dateCreated TIMESTAMP DEFAULT NOW(),
    dateModified TIMESTAMP,
    modifiedBy BIGINT REFERENCES company_account(id)
);

CREATE TABLE IF NOT EXISTS vehicles_maintenance (
    maintenanceId BIGINT REFERENCES maintenance_records(id),
    vehicleId BIGINT REFERENCES vehicles(id),
    PRIMARY KEY (maintenanceId, vehicleId)
);

CREATE TABLE IF NOT EXISTS company_vehicles (
    companyId BIGINT REFERENCES companies(id),
    vehicleId BIGINT REFERENCES vehicles(id),
    PRIMARY KEY (companyId, vehicleId)
);

-- ORDERS + PROGRESS

CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    "pick-up" POINT,
    delivery POINT,
    loadType load_type NOT NULL,
    departureTime TIMESTAMP,
    arrivalTime TIMESTAMP,
    status VARCHAR,
    vehicleId BIGINT REFERENCES vehicles(id),
    driverId BIGINT REFERENCES company_account(id)
);

CREATE TABLE IF NOT EXISTS progress (
    id BIGSERIAL PRIMARY KEY,
    orderId BIGINT REFERENCES orders(id),
    currentPos POINT,
    time TIMESTAMP DEFAULT NOW(),
    type progress_type NOT NULL,
    description JSONB
);

CREATE TABLE IF NOT EXISTS order_progress (
    orderId BIGINT REFERENCES orders(id),
    progressId BIGINT REFERENCES progress(id),
    PRIMARY KEY (orderId, progressId)
);
