# FleetMaster Logistics - Database Documentation

```
╔═══════════════════════════════════════════════════════════════════════════╗
║                                                                           ║
║                    FLEETMASTER LOGISTICS DATABASE                         ║
║                         Technical Specification                           ║
║                            Version 1.0.0                                  ║
║                                                                           ║
╚═══════════════════════════════════════════════════════════════════════════╝
```

## Table of Contents

1. [Overview](#overview)
2. [Database Schema](#database-schema)
3. [Stored Procedures](#stored-procedures)
4. [Views](#views)
5. [Triggers](#triggers)
6. [Integration Guide](#integration-guide)
7. [Usage Examples](#usage-examples)
8. [Security Considerations](#security-considerations)
9. [Maintenance & Operations](#maintenance--operations)

---

## Overview

### Purpose

FleetMaster Logistics is a comprehensive fleet management platform designed to handle:
- Company registration and user authentication
- Multi-role operational profiles
- Vehicle fleet management
- Transport order tracking
- License-based subscription management
- Company referral programs

### Technology Stack

- **Database:** PostgreSQL 16+
- **Schema Files:** 5 SQL files (tables, data, views, procedures, triggers)
- **Initialization:** Automated via Docker Compose
- **Backend Integration:** JDBC-compatible stored procedures

### Key Features

✅ Account registration with email verification  
✅ Login security with automatic blocking (3 failed attempts)  
✅ Password recovery system  
✅ Multi-role operational profiles  
✅ Real-time order tracking  
✅ License-based resource limits  
✅ Referral discount system  

---

## Database Schema

### Enumeration Types

```sql
-- User Roles (Operational Profiles)
user_role: 'ADMIN' | 'MANAGER' | 'DRIVER' | 'VIEWER'

-- Invite Statuses
invite_status: 'PENDING' | 'ACCEPTED' | 'EXPIRED'

-- Referral Statuses
referral_status: 'PENDING' | 'ACCEPTED' | 'EXPIRED'

-- License Tiers
license_name: 'BASIC' | 'PROFESSIONAL' | 'ENTERPRISE'

-- Order Statuses
order_status: 'PENDING' | 'IN_TRANSIT' | 'DELIVERED' | 'CANCELED'

-- Maintenance Statuses
maintenance_status: 'SCHEDULED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELED'

-- Vehicle Types
vehicle_type: 'LORRY' | 'VAN' | 'REFRIGERATED_TRUCK'

-- Load Types
load_type: 'NORMAL' | 'REFRIGERATED' | 'HAZARDOUS'

-- Progress Types
progress_type: 'LOADING' | 'DEPARTURE' | 'STOPOVER' | 'BREAK' | 'FUEL' |
               'STOP' | 'INSPECTION' | 'DEVIATION' | 'BREAKDOWN' |
               'INTERRUPTION' | 'UNLOADING' | 'ARRIVAL' | 'COMPLETION'
```

### Core Tables

#### 1. `company`
Primary entity representing a logistics company.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Unique company identifier |
| `name` | VARCHAR | NOT NULL | Company name |
| `license` | BIGINT | FOREIGN KEY → license_levels(id) | Current license level |
| `discount_received` | BOOLEAN | DEFAULT FALSE | One-time referral discount flag |

**Indexes:**
- Primary key on `id`

---

#### 2. `company_account`
User accounts within companies. Supports multiple operational profiles via roles array.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Unique user identifier |
| `company_id` | BIGINT | FOREIGN KEY → company(id) | Parent company |
| `username` | VARCHAR | NOT NULL, UNIQUE | Login username |
| `email` | VARCHAR | NOT NULL, UNIQUE | Email address |
| `password_hash` | VARCHAR | NOT NULL | Hashed password |
| `first_name` | VARCHAR | NULL | User first name |
| `last_name` | VARCHAR | NULL | User last name |
| `is_active` | BOOLEAN | DEFAULT TRUE | Account activation status |
| `date_created` | TIMESTAMP | DEFAULT NOW() | Registration timestamp |
| `roles` | user_role[] | NOT NULL, DEFAULT [] | Operational roles array |
| `preferences` | JSONB | NULL | User preferences (UI settings, etc.) |

**Preferences Structure:**
```json
{
  "language": "en",
  "notifications": true,
  "theme": "light"
}
```

**Indexes:**
- Primary key on `id`
- Unique on `username`
- Unique on `email`
- Foreign key on `company_id`

---

#### 3. `subscriptions`
Company subscription management with license tiers.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Subscription identifier |
| `company_id` | BIGINT | FOREIGN KEY → company(id) | Subscribed company |
| `license_id` | BIGINT | FOREIGN KEY → license_levels(id) | License tier |
| `start_date` | DATE | NOT NULL | Subscription start |
| `end_date` | DATE | NULL | Subscription end (NULL = active) |
| `is_trial` | BOOLEAN | DEFAULT FALSE | Trial period flag |
| `discount_rate` | BIGINT | DEFAULT 0 | Discount percentage |

**Business Rules:**
- Active subscription: `end_date IS NULL OR end_date > CURRENT_DATE`
- New registrations get 90-day trial automatically
- Only one active subscription per company

---

#### 4. `license_levels`
License tier definitions and limits.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | SERIAL | PRIMARY KEY | License identifier |
| `name` | license_name | NOT NULL | Tier name |
| `max_vehicles` | BIGINT | NULL | Vehicle limit (NULL = unlimited) |
| `max_drivers` | BIGINT | NULL | Driver limit (NULL = unlimited) |
| `max_assignments` | BIGINT | NULL | Assignment limit |
| `monthly_fee` | DOUBLE PRECISION | NULL | Monthly cost |

**Default Tiers:**

| Tier | Max Vehicles | Max Drivers | Max Assignments | Monthly Fee |
|------|--------------|-------------|-----------------|-------------|
| BASIC | 10 | 10 | 50 | €99.99 |
| PROFESSIONAL | 50 | 50 | 500 | €299.99 |
| ENTERPRISE | ∞ | ∞ | ∞ | €999.99 |

---

#### 5. `vehicles`
Fleet vehicles with sensor data support.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | SERIAL | PRIMARY KEY | Vehicle identifier |
| `load_capacity` | BIGINT | NULL | Capacity in kg |
| `type` | vehicle_type | NOT NULL | Vehicle classification |
| `year_of_manufacture` | DATE | NULL | Manufacturing date |
| `load_type` | load_type[] | NOT NULL | Supported load types |
| `maintenance_id` | BIGINT | NULL | Current maintenance |
| `last_odometer` | BIGINT | NULL | Last recorded mileage |
| `sensor_data` | JSONB | NULL | Real-time sensor readings |

**Sensor Data Structure:**
```json
{
  "gps_enabled": true,
  "temperature_sensor": false,
  "fuel_sensor": true,
  "last_check": "2025-12-15T14:28:04.433Z"
}
```

---

#### 6. `orders`
Transport orders linking vehicles, drivers, and routes.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Order identifier |
| `pick_up` | POINT | NULL | Pickup coordinates (lat, lon) |
| `delivery` | POINT | NULL | Delivery coordinates |
| `load_type` | load_type | NOT NULL | Cargo type |
| `departure_time` | TIMESTAMP | NULL | Planned departure |
| `arrival_time` | TIMESTAMP | NULL | Planned arrival |
| `status` | order_status | NOT NULL, DEFAULT 'PENDING' | Current status |
| `vehicle_id` | BIGINT | FOREIGN KEY → vehicles(id) | Assigned vehicle |
| `driver_id` | BIGINT | FOREIGN KEY → company_account(id) | Assigned driver |

**Status Transitions:**
```
PENDING → IN_TRANSIT → DELIVERED
   ↓           ↓
CANCELED   CANCELED
```

---

#### 7. `progress`
Order progress tracking with geolocation.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `id` | BIGSERIAL | PRIMARY KEY | Progress identifier |
| `order_id` | BIGINT | FOREIGN KEY → orders(id) | Parent order |
| `current_pos` | POINT | NULL | Current GPS position |
| `time` | TIMESTAMP | DEFAULT NOW() | Event timestamp |
| `type` | progress_type | NOT NULL | Event type |
| `description` | JSONB | NULL | Additional event data |

---

## Stored Procedures

### Authentication & Account Management

#### `sp_register_company`

**Purpose:** Creates a new company with initial subscription.

**Signature:**
```sql
sp_register_company(
    p_company_name VARCHAR,
    p_license license_name DEFAULT 'BASIC',
    p_is_trial BOOLEAN DEFAULT TRUE
) RETURNS TABLE (
    company_id BIGINT,
    subscription_id BIGINT,
    start_date DATE,
    end_date DATE,
    license_id BIGINT
)
```

**Parameters:**
- `p_company_name` - Unique company name (case-insensitive check)
- `p_license` - License tier ('BASIC', 'PROFESSIONAL', 'ENTERPRISE')
- `p_is_trial` - If TRUE, creates 90-day trial subscription

**Returns:**
- `company_id` - Created company ID
- `subscription_id` - Created subscription ID
- `start_date` - Subscription start (today)
- `end_date` - Trial end (today + 90 days) or NULL
- `license_id` - License level ID

**Exceptions:**
- Company name already exists
- License level not found

**Example:**
```sql
-- Register company with BASIC trial
SELECT * FROM sp_register_company('Acme Logistics', 'BASIC', TRUE);

-- Register with ENTERPRISE (no trial)
SELECT * FROM sp_register_company('Global Freight Inc', 'ENTERPRISE', FALSE);
```

**Business Logic:**
1. Validates unique company name (case-insensitive)
2. Creates company record
3. Creates subscription with 90-day trial if `p_is_trial = TRUE`
4. Sets `company.license` to selected tier

---

#### `sp_register_user`

**Purpose:** Registers new user and generates activation token.

**Signature:**
```sql
sp_register_user(
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
)
```

**Parameters:**
- `p_company_id` - Existing company ID
- `p_username` - Unique username
- `p_email` - Unique email address
- `p_password_hash` - Pre-hashed password (use bcrypt/argon2)
- `p_first_name` - Optional first name
- `p_last_name` - Optional last name

**Returns:**
- `user_id` - Created user ID
- `activation_token` - Verification token (send via email)
- `expires_at` - Token expiration (24 hours)

**Exceptions:**
- Company does not exist
- Email already registered
- Username already taken

**Example:**
```sql
SELECT * FROM sp_register_user(
    1,                              -- company_id
    'jdoe',                         -- username
    'john.doe@acmelogistics.com',  -- email
    '$2a$10$...',                   -- password_hash
    'John',                         -- first_name
    'Doe'                           -- last_name
);
-- Returns: {user_id: 37, activation_token: "3f2a1b...", expires_at: "2026-01-13 14:30:00"}
```

**Security Notes:**
- User created with `is_active = FALSE`
- Token stored in `user_invites` with `status = 'PENDING'`
- Activation required before login

---

#### `sp_activate_user`

**Purpose:** Activates user account via verification token.

**Signature:**
```sql
sp_activate_user(
    p_token VARCHAR
) RETURNS TABLE (
    user_id BIGINT,
    company_id BIGINT,
    email VARCHAR,
    activated BOOLEAN
)
```

**Parameters:**
- `p_token` - Activation token from registration

**Returns:**
- `user_id` - Activated user ID
- `company_id` - User's company
- `email` - User's email
- `activated` - Always TRUE on success

**Exceptions:**
- Invalid or expired token
- No user found for token email

**Example:**
```sql
SELECT * FROM sp_activate_user('3f2a1b...');
-- Returns: {user_id: 37, company_id: 1, email: "john.doe@...", activated: true}
```

**Business Logic:**
1. Validates token exists and `status = 'PENDING'`
2. Finds user by email from token
3. Sets `company_account.is_active = TRUE`
4. Marks token as `status = 'ACCEPTED'`

---

#### `sp_attempt_login`

**Purpose:** Authenticates user with automatic blocking after 3 failed attempts.

**Signature:**
```sql
sp_attempt_login(
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
)
```

**Parameters:**
- `p_identity` - Username or email (case-insensitive)
- `p_password_hash` - Hashed password to verify

**Returns:**
- `user_id` - User ID (NULL if not found)
- `company_id` - Company ID
- `username` - Username
- `email` - Email address
- `is_successful` - Login success status
- `is_blocked` - Account blocked status (3+ failures)
- `failed_attempts` - Failed attempt count (last hour)
- `reason` - Failure reason or NULL on success

**Failure Reasons:**
- `USER_NOT_FOUND` - Identity doesn't exist
- `NOT_ACTIVATED` - Account not activated
- `ACCOUNT_BLOCKED` - 3+ failed attempts in last hour
- `INVALID_PASSWORD` - Wrong password
- `NULL` - Success

**Example:**
```sql
-- Successful login
SELECT * FROM sp_attempt_login('jdoe', '$2a$10$...');
-- Returns: {user_id: 37, is_successful: true, is_blocked: false, failed_attempts: 0, reason: null}

-- Failed login (wrong password)
SELECT * FROM sp_attempt_login('jdoe', 'wronghash');
-- Returns: {user_id: 37, is_successful: false, is_blocked: false, failed_attempts: 1, reason: 'INVALID_PASSWORD'}

-- Blocked after 3rd failure
SELECT * FROM sp_attempt_login('jdoe', 'wronghash');
-- Returns: {user_id: 37, is_successful: false, is_blocked: true, failed_attempts: 3, reason: 'ACCOUNT_BLOCKED'}
```

**Business Logic:**
1. Finds user by email or username
2. Checks activation status
3. Checks blocking via `vw_auth_check` (≥3 failures in last hour)
4. Verifies password
5. Records login attempt in `logins` table
6. Re-checks block status after attempt

**Auto-Unblocking:**
- Block automatically expires after 1 hour from the first failed attempt
- No manual intervention required

---

#### `sp_request_password_recovery`

**Purpose:** Generates password recovery token.

**Signature:**
```sql
sp_request_password_recovery(
    p_email VARCHAR
) RETURNS TABLE (
    user_id BIGINT,
    recovery_token UUID,
    expires_at TIMESTAMP
)
```

**Parameters:**
- `p_email` - User's email address

**Returns:**
- `user_id` - User ID
- `recovery_token` - UUID token (send via email)
- `expires_at` - Token expiration (1 hour)

**Exceptions:**
- No active user found with email

**Example:**
```sql
SELECT * FROM sp_request_password_recovery('john.doe@acmelogistics.com');
-- Returns: {user_id: 37, recovery_token: "550e8400-...", expires_at: "2026-01-12 15:30:00"}
```

**Business Logic:**
1. Validates active user exists
2. Deactivates any existing recovery tokens
3. Creates new token with 1-hour expiration

---

#### `sp_reset_password`

**Purpose:** Resets password using recovery token.

**Signature:**
```sql
sp_reset_password(
    p_token UUID,
    p_new_password_hash VARCHAR
) RETURNS TABLE (
    user_id BIGINT,
    email VARCHAR,
    password_reset BOOLEAN
)
```

**Parameters:**
- `p_token` - Recovery token from email
- `p_new_password_hash` - New hashed password

**Returns:**
- `user_id` - User ID
- `email` - User email
- `password_reset` - Always TRUE on success

**Exceptions:**
- Invalid or expired token

**Example:**
```sql
SELECT * FROM sp_reset_password(
    '550e8400-...'::UUID,
    '$2a$10$newhashedpassword...'
);
-- Returns: {user_id: 37, email: "john.doe@...", password_reset: true}
```

**Business Logic:**
1. Validates token is active and not expired
2. Updates `company_account.password_hash`
3. Deactivates recovery token

---

### Operational Profile Management

#### `sp_assign_role`

**Purpose:** Assigns operational role to user (can have multiple).

**Signature:**
```sql
sp_assign_role(
    p_user_id BIGINT,
    p_role user_role
) RETURNS BOOLEAN
```

**Parameters:**
- `p_user_id` - User ID
- `p_role` - Role to assign ('ADMIN', 'MANAGER', 'DRIVER', 'VIEWER')

**Returns:**
- `TRUE` on success

**Example:**
```sql
-- Assign DRIVER role
SELECT sp_assign_role(37, 'DRIVER');

-- User can have multiple roles
SELECT sp_assign_role(37, 'MANAGER');

-- Roles array now: ['DRIVER', 'MANAGER']
```

**Business Logic:**
- Idempotent: Won't add duplicate roles
- DRIVER role triggers license limit check via trigger

---

#### `sp_remove_role`

**Purpose:** Removes operational role from user.

**Signature:**
```sql
sp_remove_role(
    p_user_id BIGINT,
    p_role user_role
) RETURNS BOOLEAN
```

**Parameters:**
- `p_user_id` - User ID
- `p_role` - Role to remove

**Returns:**
- `TRUE` if role was removed, `FALSE` if not found

**Example:**
```sql
SELECT sp_remove_role(37, 'DRIVER');
-- Removes DRIVER from roles array
```

---

#### `sp_update_work_preferences`

**Purpose:** Sets user's location and work preferences.

**Signature:**
```sql
sp_update_work_preferences(
    p_user_id BIGINT,
    p_location VARCHAR DEFAULT NULL,
    p_work_preferences JSONB DEFAULT NULL
) RETURNS BOOLEAN
```

**Parameters:**
- `p_user_id` - User ID
- `p_location` - Location string (e.g., "Warehouse A", "Northern Region")
- `p_work_preferences` - JSONB preferences object

**Returns:**
- `TRUE` on success

**Example:**
```sql
-- Update preferences (stored procedure supports location and work_preferences)
SELECT sp_update_work_preferences(
    37,
    'Northern Region',
    '{\"min_load_capacity\": 5000}'::jsonb
);

-- Update only location
SELECT sp_update_work_preferences(37, 'Warehouse B', NULL);
```

**Note:** Test data uses preferences for UI settings (language, notifications, theme).  
The stored procedure supports extending preferences with location and operational filters.

---

### Transport Order Management

#### `sp_create_order`

**Purpose:** Creates new transport order.

**Signature:**
```sql
sp_create_order(
    p_vehicle_id BIGINT,
    p_driver_id BIGINT,
    p_pick_up POINT,
    p_delivery POINT,
    p_load_type load_type,
    p_departure_time TIMESTAMP DEFAULT NULL,
    p_arrival_time TIMESTAMP DEFAULT NULL
) RETURNS BIGINT
```

**Parameters:**
- `p_vehicle_id` - Assigned vehicle ID
- `p_driver_id` - Assigned driver (company_account.id)
- `p_pick_up` - Pickup coordinates (latitude, longitude)
- `p_delivery` - Delivery coordinates
- `p_load_type` - Cargo type
- `p_departure_time` - Planned departure (NULL = TBD)
- `p_arrival_time` - Planned arrival (NULL = TBD)

**Returns:**
- Created order ID

**Exceptions:**
- Vehicle does not exist
- Driver does not exist

**Example:**
```sql
SELECT sp_create_order(
    12,                                     -- vehicle_id
    37,                                     -- driver_id
    '(52.520008, 13.404954)'::POINT,       -- Berlin pickup
    '(48.856613, 2.352222)'::POINT,        -- Paris delivery
    'REFRIGERATED',                         -- load_type
    '2026-01-15 06:00:00'::TIMESTAMP,      -- departure
    '2026-01-15 18:00:00'::TIMESTAMP       -- arrival
);
-- Returns: 101 (order_id)
```

**Initial Status:** `PENDING`

**POINT Format:**
- PostgreSQL POINT: `(latitude, longitude)`
- Example: `(52.52, 13.40)` = Berlin coordinates

---

#### `sp_add_progress`

**Purpose:** Records progress event and auto-updates order status.

**Signature:**
```sql
sp_add_progress(
    p_order_id BIGINT,
    p_position POINT,
    p_type progress_type,
    p_description JSONB DEFAULT '{}'
) RETURNS BIGINT
```

**Parameters:**
- `p_order_id` - Order ID
- `p_position` - Current GPS position
- `p_type` - Progress event type
- `p_description` - Additional event data (JSONB)

**Returns:**
- Created progress ID

**Exceptions:**
- Order does not exist

**Example:**
```sql
-- Record departure
SELECT sp_add_progress(
    101,
    '(52.520008, 13.404954)'::POINT,
    'DEPARTURE',
    '{"note": "Left depot on time", "fuel_level": 100, "temperature": 4}'::jsonb
);
-- Order status → IN_TRANSIT

-- Record stopover
SELECT sp_add_progress(
    101,
    '(50.110924, 8.682127)'::POINT,
    'STOPOVER',
    '{"duration_minutes": 30, "reason": "Refuel and rest"}'::jsonb
);

-- Record arrival
SELECT sp_add_progress(
    101,
    '(48.856613, 2.352222)'::POINT,
    'ARRIVAL',
    '{"fuel_level": 45, "temperature": 4, "on_time": true}'::jsonb
);
-- Order status → DELIVERED
```

**Auto Status Updates:**
| Progress Type | Order Status Change |
|---------------|---------------------|
| `DEPARTURE` | `PENDING` → `IN_TRANSIT` |
| `ARRIVAL` | `IN_TRANSIT` → `DELIVERED` |
| `COMPLETION` | `IN_TRANSIT` → `DELIVERED` |
| Others | No change |

---

### Subscription & Referral Management

#### `sp_activate_referral_discount`

**Purpose:** Activates one-time referral discount for both companies.

**Signature:**
```sql
sp_activate_referral_discount(
    p_inviter_company_id BIGINT,
    p_invited_company_id BIGINT,
    p_discount_rate BIGINT DEFAULT 10,
    p_validity_days BIGINT DEFAULT 90
) RETURNS BOOLEAN
```

**Parameters:**
- `p_inviter_company_id` - Company that sent invitation
- `p_invited_company_id` - Company that accepted
- `p_discount_rate` - Discount percentage (default: 10%)
- `p_validity_days` - Discount duration (default: 90 days)

**Returns:**
- `TRUE` on success

**Exceptions:**
- No accepted referral link found
- Inviter already received discount
- Invited company already received discount

**Example:**
```sql
SELECT sp_activate_referral_discount(1, 11, 10, 90);
-- Applies 10% discount to both companies for 90 days
```

**Business Logic:**
1. Validates referral link exists with `discount_status = 'ACCEPTED'`
2. Checks neither company has `discount_received = TRUE`
3. Updates active subscriptions with discount
4. Marks both companies as `discount_received = TRUE`
5. Records activation date and validity period

**One-Time Rule:**
- Each company can only receive discount once (lifetime)
- Prevents abuse of referral system

---

## Views

### `vw_auth_check`

**Purpose:** Authentication validation with automatic blocking logic.

**Columns:**
```sql
user_id             BIGINT      -- User identifier
company_id          BIGINT      -- Company identifier
email               VARCHAR     -- Email address
username            VARCHAR     -- Username
is_activated        BOOLEAN     -- Account activation status
failed_attempt_count BIGINT     -- Failed logins in last hour
is_blocked          BOOLEAN     -- TRUE if failed_attempt_count >= 3
```

**Usage:**
```sql
SELECT * FROM vw_auth_check WHERE email = 'user@example.com';
```

**Blocking Logic:**
- Counts failed logins from `logins` table where `is_successful = FALSE`
- Time window: Last 1 hour
- Threshold: 3 failed attempts
- Auto-unblocks after 1 hour from first failure

---

### `vw_subscription_usage`

**Purpose:** Shows company subscription limits and current usage.

**Columns:**
```sql
company_id          BIGINT      -- Company identifier
company_name        VARCHAR     -- Company name
license_tier        license_name -- License level
max_vehicles        BIGINT      -- Vehicle limit (NULL = unlimited)
max_drivers         BIGINT      -- Driver limit (NULL = unlimited)
current_vehicles    BIGINT      -- Current vehicle count
current_drivers     BIGINT      -- Current driver count
```

**Usage:**
```sql
-- Check subscription status for company
SELECT * FROM vw_subscription_usage WHERE company_id = 1;

-- Find companies near limits
SELECT * FROM vw_subscription_usage 
WHERE current_vehicles >= max_vehicles * 0.9;
```

**Business Logic:**
- Shows only active subscriptions (end_date NULL or future)
- Current counts are real-time

---

### `vw_order_tracking`

**Purpose:** Active order monitoring with latest progress.

**Columns:**
```sql
order_id            BIGINT      -- Order identifier
company_id          BIGINT      -- Company owning the vehicle
driver_id           BIGINT      -- Assigned driver
vehicle_id          BIGINT      -- Assigned vehicle
order_status        order_status -- Current status
pick_up             POINT       -- Pickup coordinates
delivery            POINT       -- Delivery coordinates
last_progress_event progress_type -- Latest progress type
last_known_location POINT       -- Latest GPS position
last_update_time    TIMESTAMP   -- Latest progress timestamp
```

**Filtering:**
- Excludes `DELIVERED` and `CANCELED` orders
- Shows only active orders

**Usage:**
```sql
-- Get active orders for company
SELECT * FROM vw_order_tracking WHERE company_id = 1;

-- Find orders with no recent updates
SELECT * FROM vw_order_tracking 
WHERE last_update_time < NOW() - INTERVAL '2 hours';
```

**Use Cases:**
- Real-time fleet monitoring
- Driver order lists
- Operational dashboards

---

### `vw_fleet_status`

**Purpose:** Vehicle fleet overview with maintenance status.

**Columns:**
```sql
company_id              BIGINT          -- Company identifier
vehicle_id              BIGINT          -- Vehicle identifier
vehicle_type            vehicle_type    -- Vehicle classification
load_capacity           BIGINT          -- Capacity in kg
load_type               load_type[]     -- Supported cargo types
sensor_data             JSONB           -- Real-time sensors
last_odometer           BIGINT          -- Last mileage reading
current_maintenance_status maintenance_status -- Latest maintenance status
```

**Usage:**
```sql
-- Get fleet for company
SELECT * FROM vw_fleet_status WHERE company_id = 1;

-- Find refrigerated trucks
SELECT * FROM vw_fleet_status 
WHERE 'REFRIGERATED' = ANY(load_type);

-- Check vehicles needing maintenance
SELECT * FROM vw_fleet_status 
WHERE current_maintenance_status IN ('SCHEDULED', 'IN_PROGRESS');
```

---

### `vw_user_profiles`

**Purpose:** User account overview with roles and preferences.

**Columns:**
```sql
user_id         BIGINT          -- User identifier
company_id      BIGINT          -- Company identifier
username        VARCHAR         -- Username
roles           user_role[]     -- Operational roles array
preferences     JSONB           -- User preferences (UI settings, location, filters)
first_name      VARCHAR         -- First name
last_name       VARCHAR         -- Last name
```

**Usage:**
```sql
-- Get all users for company
SELECT * FROM vw_user_profiles WHERE company_id = 1;

-- Find drivers
SELECT * FROM vw_user_profiles WHERE 'DRIVER' = ANY(roles);

-- Find users by theme preference
SELECT * FROM vw_user_profiles 
WHERE preferences->>'theme' = 'dark';
```

---

## Triggers

### `trg_enforce_vehicle_limit`

**Purpose:** Enforces max vehicle limit based on license.

**Trigger Event:** `BEFORE INSERT ON company_vehicles`

**Logic:**
1. Gets `max_vehicles` from active subscription
2. Counts current vehicles for company
3. Raises exception if limit reached

**Exception Message:**
```
Vehicle limit exceeded. Company 1 is limited to 10 vehicles (license: BASIC)
```

**Example:**
```sql
-- Company 1 has BASIC license (10 vehicles max)
-- Currently has 10 vehicles

INSERT INTO company_vehicles VALUES (1, 99);
-- ERROR: Vehicle limit exceeded. Company 1 is limited to 10 vehicles (license: BASIC)
```

**Bypass:** None. Upgrade license to add more vehicles.

---

### `trg_enforce_driver_limit`

**Purpose:** Enforces max driver limit based on license.

**Trigger Event:** `BEFORE INSERT OR UPDATE OF roles ON company_account`

**Logic:**
1. Detects when DRIVER role is being added
2. Gets `max_drivers` from active subscription
3. Counts current active drivers
4. Raises exception if limit reached

**Exception Message:**
```
Driver limit exceeded. Company 1 is limited to 10 drivers (license: BASIC)
```

**Example:**
```sql
-- Company 1 has BASIC license (10 drivers max)
-- Currently has 10 drivers

SELECT sp_assign_role(38, 'DRIVER');
-- ERROR: Driver limit exceeded. Company 1 is limited to 10 drivers (license: BASIC)
```

**Notes:**
- Only active users count toward limit
- Removing DRIVER role frees up slot
- Upgrade license to add more drivers

---

## Integration Guide

### Java Backend Integration

#### JDBC Setup

```java
// application.properties
spring.datasource.url=jdbc:postgresql://postgres:5432/dataprocessingapi_db
spring.datasource.username=${POSTGRES_USER}
spring.datasource.password=${POSTGRES_PASSWORD}
```

#### Calling Stored Procedures

**Example 1: User Registration**
```java
@Repository
public class AuthRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public UserRegistration registerUser(
        Long companyId, 
        String username, 
        String email, 
        String passwordHash
    ) {
        String sql = "SELECT * FROM sp_register_user(?, ?, ?, ?, NULL, NULL)";
        
        return jdbcTemplate.queryForObject(sql, 
            (rs, rowNum) -> new UserRegistration(
                rs.getLong("user_id"),
                rs.getString("activation_token"),
                rs.getTimestamp("expires_at")
            ),
            companyId, username, email, passwordHash
        );
    }
}
```

**Example 2: Login Attempt**
```java
public LoginResult attemptLogin(String identity, String passwordHash) {
    String sql = "SELECT * FROM sp_attempt_login(?, ?)";
    
    return jdbcTemplate.queryForObject(sql,
        (rs, rowNum) -> new LoginResult(
            rs.getLong("user_id"),
            rs.getLong("company_id"),
            rs.getBoolean("is_successful"),
            rs.getBoolean("is_blocked"),
            rs.getLong("failed_attempts"),
            rs.getString("reason")
        ),
        identity, passwordHash
    );
}
```

**Example 3: Creating Orders**
```java
public Long createOrder(OrderRequest request) {
    String sql = "SELECT sp_create_order(?, ?, ?, ?, ?::load_type, ?, ?)";
    
    PGpoint pickUp = new PGpoint(request.getPickupLat(), request.getPickupLon());
    PGpoint delivery = new PGpoint(request.getDeliveryLat(), request.getDeliveryLon());
    
    return jdbcTemplate.queryForObject(sql, Long.class,
        request.getVehicleId(),
        request.getDriverId(),
        pickUp,
        delivery,
        request.getLoadType(),
        request.getDepartureTime(),
        request.getArrivalTime()
    );
}
```

#### Working with JSONB

```java
import org.postgresql.util.PGobject;
import com.fasterxml.jackson.databind.ObjectMapper;

public void updatePreferences(Long userId, WorkPreferences prefs) {
    ObjectMapper mapper = new ObjectMapper();
    PGobject jsonObject = new PGobject();
    jsonObject.setType("jsonb");
    jsonObject.setValue(mapper.writeValueAsString(prefs));
    
    String sql = "SELECT sp_update_work_preferences(?, ?, ?::jsonb)";
    jdbcTemplate.queryForObject(sql, Boolean.class, 
        userId, prefs.getLocation(), jsonObject);
}
```

#### Querying Views

```java
public List<OrderTracking> getActiveOrders(Long companyId) {
    String sql = "SELECT * FROM vw_order_tracking WHERE company_id = ?";
    
    return jdbcTemplate.query(sql,
        (rs, rowNum) -> new OrderTracking(
            rs.getLong("order_id"),
            rs.getString("order_status"),
            // ... map other columns
        ),
        companyId
    );
}
```

### REST API Design

**Suggested Endpoints:**

```
POST   /api/auth/register-company
POST   /api/auth/register-user
GET    /api/auth/activate?token={token}
POST   /api/auth/login
POST   /api/auth/forgot-password
POST   /api/auth/reset-password

GET    /api/users/{userId}/roles
POST   /api/users/{userId}/roles
DELETE /api/users/{userId}/roles/{role}
PUT    /api/users/{userId}/preferences

POST   /api/orders
POST   /api/orders/{orderId}/progress
GET    /api/orders/active
GET    /api/orders/{orderId}

GET    /api/fleet
GET    /api/fleet/{vehicleId}

GET    /api/subscription
POST   /api/subscription/referral/activate
```

---

## Usage Examples

### Complete User Registration Flow

```sql
-- Step 1: Register company
SELECT * FROM sp_register_company('Acme Logistics');
-- Returns: company_id = 11

-- Step 2: Register user
SELECT * FROM sp_register_user(
    11, 
    'jdoe', 
    'john.doe@acmelogistics.com',
    '$2a$10$hashedpassword...',
    'John',
    'Doe'
);
-- Returns: user_id = 37, activation_token = "abc123..."

-- Step 3: User clicks email link, activate account
SELECT * FROM sp_activate_user('abc123...');
-- Returns: activated = true

-- Step 4: User logs in
SELECT * FROM sp_attempt_login('jdoe', '$2a$10$hashedpassword...');
-- Returns: is_successful = true
```

### Setting Up Operational Profile

```sql
-- Assign multiple roles
SELECT sp_assign_role(37, 'MANAGER');
SELECT sp_assign_role(37, 'DRIVER');

-- Verify
SELECT * FROM vw_user_profiles WHERE user_id = 37;
```

### Creating and Tracking Order

```sql
-- Create order
SELECT sp_create_order(
    12,                                     -- vehicle_id
    37,                                     -- driver_id
    '(52.520008, 13.404954)'::POINT,       -- Berlin
    '(48.856613, 2.352222)'::POINT,        -- Paris
    'REFRIGERATED',
    '2026-01-15 06:00:00',
    '2026-01-15 18:00:00'
);
-- Returns: order_id = 101

-- Record departure
SELECT sp_add_progress(
    101,
    '(52.520008, 13.404954)'::POINT,
    'DEPARTURE',
    NULL
);

-- Check order status
SELECT * FROM vw_order_tracking WHERE order_id = 101;
-- Shows: status = IN_TRANSIT, last_progress = DEPARTURE

-- Record stopover
SELECT sp_add_progress(
    101,
    '(50.110924, 8.682127)'::POINT,
    'STOPOVER',
    NULL
);

-- Record arrival
SELECT sp_add_progress(
    101,
    '(48.856613, 2.352222)'::POINT,
    'ARRIVAL',
    NULL
);

-- Verify completion
SELECT * FROM vw_order_tracking WHERE order_id = 101;
-- No results (order is DELIVERED, excluded from view)

-- See all progress
SELECT * FROM progress WHERE order_id = 101 ORDER BY time;
```

### Password Recovery Flow

```sql
-- User requests recovery
SELECT * FROM sp_request_password_recovery('john.doe@acmelogistics.com');
-- Returns: recovery_token = "550e8400-..."

-- User clicks email link, resets password
SELECT * FROM sp_reset_password(
    '550e8400-...'::UUID,
    '$2a$10$newhashedpassword...'
);
-- Returns: password_reset = true

-- User can now login with new password
SELECT * FROM sp_attempt_login('jdoe', '$2a$10$newhashedpassword...');
```

### Monitoring Subscription Usage

```sql
-- Check current usage
SELECT * FROM vw_subscription_usage WHERE company_id = 1;
-- Shows: license_tier = BASIC, max_vehicles = 10, current_vehicles = 8

-- Try to exceed limit
INSERT INTO company_vehicles VALUES (1, 99);
-- ERROR: Vehicle limit exceeded

-- Upgrade to PROFESSIONAL
INSERT INTO subscriptions (company_id, license_id, start_date, is_trial)
SELECT 1, id, CURRENT_DATE, FALSE 
FROM license_levels WHERE name = 'PROFESSIONAL';

-- Update old subscription end date
UPDATE subscriptions 
SET end_date = CURRENT_DATE - INTERVAL '1 day'
WHERE company_id = 1 AND end_date IS NULL;

-- Now can add more vehicles (limit is 50)
INSERT INTO company_vehicles VALUES (1, 99);
-- Success
```

### Referral Discount Activation

```sql
-- Company 1 invites Company 11 (manual referral link creation)
INSERT INTO referral_links (
    inviter_company_id, 
    invited_company_id, 
    invitation_code,
    discount_status
) VALUES (1, 11, 'INVITE2026', 'ACCEPTED');

-- Activate discount
SELECT sp_activate_referral_discount(1, 11, 10, 90);
-- Both companies get 10% discount for 90 days

-- Verify
SELECT * FROM subscriptions WHERE company_id IN (1, 11);
-- Shows: discount_rate = 10

SELECT * FROM company WHERE id IN (1, 11);
-- Shows: discount_received = true (can't get discount again)
```

---

## Security Considerations

### Password Storage

**Requirements:**
- Use bcrypt (cost factor ≥12) or Argon2id
- Never store plaintext passwords
- Hash passwords before calling stored procedures

```java
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
String hashedPassword = encoder.encode(rawPassword);
```

### SQL Injection Protection

**Built-in Protection:**
- All stored procedures use parameterized queries
- Use JDBC PreparedStatement or named parameters
- Never concatenate user input into SQL

```java
// ✅ SAFE
jdbcTemplate.queryForObject(
    "SELECT * FROM sp_attempt_login(?, ?)", 
    loginResultMapper, 
    userInput, 
    password
);

// ❌ DANGEROUS - Never do this
String sql = "SELECT * FROM sp_attempt_login('" + userInput + "', '" + password + "')";
```

### Token Security

**Activation Tokens:**
- 24-hour expiration
- Single-use (marked ACCEPTED after use)
- Secure Random String (derived from UUID v4)

**Recovery Tokens:**
- 1-hour expiration
- UUID v4 (cryptographically secure)
- Single-use (marked is_active = FALSE after use)

### Account Blocking

**Automatic Protection:**
- 3 failed login attempts → block for 1 hour
- No manual unlock needed (auto-expires)
- Failed attempts tracked per user, not IP

**Prevention:**
```sql
-- Check if user is blocked before attempt
SELECT is_blocked FROM vw_auth_check WHERE email = 'user@example.com';
```

### Role-Based Access

**Implementation Suggestions:**
```java
@PreAuthorize("hasRole('ADMIN')")
public void adminOnlyMethod() { }

@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public void managerMethod() { }
```

### Data Privacy

**GDPR Considerations:**
- Implement user deletion cascade
- Add audit logs for data access
- Encrypt sensitive fields (e.g., email)
- Add data export functionality

---

## Maintenance & Operations

### Database Initialization

**Docker Compose Setup:**
```yaml
volumes:
  - ./backend/sql/tables.sql:/docker-entrypoint-initdb.d/01-tables.sql:ro
  - ./backend/sql/data.sql:/docker-entrypoint-initdb.d/02-data.sql:ro
  - ./backend/sql/views.sql:/docker-entrypoint-initdb.d/03-views.sql:ro
  - ./backend/sql/sp.sql:/docker-entrypoint-initdb.d/04-sp.sql:ro
  - ./backend/sql/triggers.sql:/docker-entrypoint-initdb.d/05-triggers.sql:ro
```

**Execution Order:**
1. `tables.sql` - Schema and enums
2. `data.sql` - Test data
3. `views.sql` - Views
4. `sp.sql` - Stored procedures
5. `triggers.sql` - Triggers

### Reset Database

```bash
# Stop and remove volumes
docker compose down -v

# Restart (auto-initializes from SQL files)
docker compose up -d

# Wait for health check
docker compose ps
```

### Backup Strategy

**Full Backup:**
```bash
docker exec dataprocessingapi_db pg_dump \
  -U $POSTGRES_USER \
  -d dataprocessingapi_db \
  -F c \
  -f /tmp/backup.dump

docker cp dataprocessingapi_db:/tmp/backup.dump ./backup.dump
```

**Schema Only:**
```bash
docker exec dataprocessingapi_db pg_dump \
  -U $POSTGRES_USER \
  -d dataprocessingapi_db \
  --schema-only \
  -f /tmp/schema.sql
```

**Restore:**
```bash
docker cp backup.dump dataprocessingapi_db:/tmp/

docker exec dataprocessingapi_db pg_restore \
  -U $POSTGRES_USER \
  -d dataprocessingapi_db \
  -c \
  /tmp/backup.dump
```

### Performance Monitoring

**Query Performance:**
```sql
-- Show slow queries
SELECT query, mean_exec_time, calls
FROM pg_stat_statements
ORDER BY mean_exec_time DESC
LIMIT 10;

-- View statistics
SELECT * FROM pg_stat_user_tables;
```

**Index Usage:**
```sql
-- Find unused indexes
SELECT schemaname, tablename, indexname
FROM pg_indexes
WHERE schemaname = 'public'
  AND indexname NOT IN (
    SELECT indexrelname FROM pg_stat_user_indexes
    WHERE idx_scan > 0
  );
```

### Recommended Indexes

```sql
-- Authentication queries
CREATE INDEX idx_company_account_email ON company_account(LOWER(email));
CREATE INDEX idx_company_account_username ON company_account(LOWER(username));

-- Login tracking (already exists in tables.sql)
CREATE INDEX idx_logins_user_time ON logins(user_id, time DESC);

-- Order tracking
CREATE INDEX idx_orders_status ON orders(status) WHERE status NOT IN ('DELIVERED', 'CANCELED');
CREATE INDEX idx_progress_order_time ON progress(order_id, time DESC);

-- Fleet management
CREATE INDEX idx_company_vehicles_company ON company_vehicles(company_id);

-- JSONB queries (if frequently filtering by preferences)
CREATE INDEX idx_account_preferences_gin ON company_account USING gin(preferences);
```

### Monitoring Checklist

Daily:
- [ ] Check failed login rates
- [ ] Monitor active order count
- [ ] Verify subscription expirations

Weekly:
- [ ] Review query performance
- [ ] Check database size growth
- [ ] Audit user registrations

Monthly:
- [ ] Backup verification
- [ ] Index maintenance (REINDEX)
- [ ] Table statistics update (ANALYZE)

### Troubleshooting

**Problem: Login always blocked**
```sql
-- Check failed login history
SELECT user_id, time, is_successful
FROM logins
WHERE user_id = 37
  AND time > NOW() - INTERVAL '1 hour'
ORDER BY time DESC;

-- Manually clear old failed attempts (emergency only)
DELETE FROM logins
WHERE user_id = 37
  AND is_successful = FALSE
  AND time < NOW() - INTERVAL '1 hour';
```

**Problem: Can't add vehicles**
```sql
-- Check current usage
SELECT * FROM vw_subscription_usage WHERE company_id = 1;

-- Verify active subscription
SELECT * FROM subscriptions
WHERE company_id = 1
  AND (end_date IS NULL OR end_date > CURRENT_DATE);
```

**Problem: Order not showing in vw_order_tracking**
```sql
-- Check order status
SELECT id, status FROM orders WHERE id = 101;

-- View excludes DELIVERED and CANCELED
-- If status is DELIVERED, that's expected behavior

-- See all orders including completed
SELECT * FROM orders WHERE id = 101;
```

---

## Appendix

### SQL File Reference

| File | Purpose | Lines | Dependencies |
|------|---------|-------|--------------|
| `tables.sql` | Schema definition | ~200 | None |
| `data.sql` | Test data | ~640K | tables.sql |
| `views.sql` | Database views | ~100 | tables.sql |
| `sp.sql` | Stored procedures | ~563 | tables.sql, views.sql |
| `triggers.sql` | Limit enforcement | ~80 | tables.sql, sp.sql |

### Change Log

| Version | Date | Changes |
|---------|------|---------|
| 1.0.0 | 2026-01-12 | Initial release |

### License Information

**Database Schema:** Proprietary - FleetMaster Logistics  
**PostgreSQL:** PostgreSQL License (similar to BSD/MIT)

---

```
╔═══════════════════════════════════════════════════════════════════════════╗
║                                                                           ║
║                     END OF DOCUMENTATION                                  ║
║                                                                           ║
║              For questions or support, contact:                           ║
║              Database Team - FleetMaster Logistics                        ║
║                                                                           ║
╚═══════════════════════════════════════════════════════════════════════════╝
```
