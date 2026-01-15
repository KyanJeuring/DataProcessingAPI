-- Create roles if they don't exist
DO
$do$
BEGIN
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
END
$do$;
