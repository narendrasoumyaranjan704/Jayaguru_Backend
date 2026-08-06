-- ============================================================================
-- Temple Donation App — PostgreSQL setup
-- Reference script for PgAdmin4 (already applied during scaffolding).
-- Run in PgAdmin4 > Query Tool while connected to the default 'postgres' db.
-- ============================================================================

-- 1) Create the application database role (safe to run repeatedly)
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'temple_app') THEN
    CREATE ROLE temple_app LOGIN PASSWORD 'temple123';
  END IF;
END
$$;

-- 2) Create the database. In PgAdmin4 run this single line separately
--    (CREATE DATABASE cannot run inside a DO block / transaction).
--    An "already exists" error here is fine.
CREATE DATABASE jayaguru;

-- 3) Grant the app role full access. Switch connection to the 'jayaguru'
--    database (right-click > Query Tool) and run:
GRANT ALL ON SCHEMA public TO temple_app;
ALTER SCHEMA public OWNER TO temple_app;

-- NOTE: The tables (donation_sheet, donation, app_user) are created
-- automatically by the Spring Boot app on first start
-- (spring.jpa.hibernate.ddl-auto = update). Sample data is seeded too.
