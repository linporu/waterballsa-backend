-- Drop database script for migrate-drop
-- This script drops all tables, types, and Liquibase tracking tables

-- Drop application tables (order matters due to foreign key constraints)
-- Current tables
DROP TABLE IF EXISTS user_mission_progress CASCADE;
DROP TABLE IF EXISTS order_items CASCADE;
DROP TABLE IF EXISTS user_journeys CASCADE;
DROP TABLE IF EXISTS orders CASCADE;
DROP TABLE IF EXISTS rewards CASCADE;
DROP TABLE IF EXISTS mission_contents CASCADE;
DROP TABLE IF EXISTS missions CASCADE;
DROP TABLE IF EXISTS chapters CASCADE;
DROP TABLE IF EXISTS journeys CASCADE;
DROP TABLE IF EXISTS access_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Legacy tables (from old migrations, may exist in some environments)
DROP TABLE IF EXISTS user_unit_submissions CASCADE;
DROP TABLE IF EXISTS user_unit_progress CASCADE;
DROP TABLE IF EXISTS user_mission_submissions CASCADE;
DROP TABLE IF EXISTS units CASCADE;
DROP TABLE IF EXISTS courses CASCADE;

-- Drop Liquibase tracking tables
DROP TABLE IF EXISTS databasechangelog CASCADE;
DROP TABLE IF EXISTS databasechangeloglock CASCADE;

-- Drop custom enum types
DROP TYPE IF EXISTS content_type CASCADE;
DROP TYPE IF EXISTS mission_access_level CASCADE;
DROP TYPE IF EXISTS progress_status CASCADE;
DROP TYPE IF EXISTS mission_status CASCADE;
DROP TYPE IF EXISTS mission_type CASCADE;
DROP TYPE IF EXISTS reward_type CASCADE;
DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS order_status CASCADE;
