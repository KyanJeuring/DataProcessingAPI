-- Create roles if they don't exist
DO
$do$
BEGIN
    -- APPLICATION ROLES (API Access)
     
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'fleet_admin') THEN

      CREATE ROLE fleet_admin WITH LOGIN PASSWORD 'admin123';
      GRANT ALL PRIVILEGES ON DATABASE dataprocessingapi_db TO fleet_admin;
      ALTER ROLE fleet_admin CREATEDB;
   END IF;

   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'fleet_app') THEN

      CREATE ROLE fleet_app WITH LOGIN PASSWORD 'app123';
      GRANT CONNECT ON DATABASE dataprocessingapi_db TO fleet_app;
      GRANT USAGE ON SCHEMA public TO fleet_app;
      GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO fleet_app;
      ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO fleet_app;
   END IF;

   -- Junior Employee: Read-only access to basic information
   -- (company accounts, registered vehicles, simple order status)
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'fleet_junior') THEN

      CREATE ROLE fleet_junior WITH LOGIN PASSWORD 'junior123';
      GRANT CONNECT ON DATABASE dataprocessingapi_db TO fleet_junior;
      GRANT USAGE ON SCHEMA public TO fleet_junior;
      
      -- Read-only access to basic tables
      GRANT SELECT ON companies TO fleet_junior;
      GRANT SELECT ON vehicles TO fleet_junior;
      GRANT SELECT ON orders TO fleet_junior;
      GRANT SELECT ON order_progress TO fleet_junior;
   END IF;

   -- Mid-level Employee: Limited changes to operational data
   -- (vehicle status, trip details) - NO financial data access
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'fleet_midlevel') THEN

      CREATE ROLE fleet_midlevel WITH LOGIN PASSWORD 'midlevel123';
      GRANT CONNECT ON DATABASE dataprocessingapi_db TO fleet_midlevel;
      GRANT USAGE ON SCHEMA public TO fleet_midlevel;
      
      -- Read access to basic information
      GRANT SELECT ON companies TO fleet_midlevel;
      GRANT SELECT ON vehicles TO fleet_midlevel;
      GRANT SELECT ON orders TO fleet_midlevel;
      GRANT SELECT ON order_progress TO fleet_midlevel;
      
      -- Update access to operational data
      GRANT UPDATE ON vehicles TO fleet_midlevel;
      GRANT UPDATE ON orders TO fleet_midlevel;
      GRANT INSERT, UPDATE ON order_progress TO fleet_midlevel;
      
      -- Read access to routes for trip management
      GRANT SELECT ON routes TO fleet_midlevel;
      GRANT UPDATE ON routes TO fleet_midlevel;
   END IF;

   -- Senior Employee: Full access to all data
   
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'fleet_senior') THEN

      CREATE ROLE fleet_senior WITH LOGIN PASSWORD 'senior123';
      GRANT CONNECT ON DATABASE dataprocessingapi_db TO fleet_senior;
      GRANT USAGE ON SCHEMA public TO fleet_senior;
      
      -- Full read/write access to all tables
      GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO fleet_senior;
      ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO fleet_senior;
      
      -- Access to sequences for inserts
      GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO fleet_senior;
      ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO fleet_senior;
   END IF;
END
$do$;