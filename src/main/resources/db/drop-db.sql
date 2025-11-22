-- Drop database script for migrate-drop
-- This script drops all tables, types, and Liquibase tracking tables

-- Drop application tables (order matters due to foreign key constraints)
DROP TABLE IF EXISTS user_mission_progress CASCADE;
DROP TABLE IF EXISTS rewards CASCADE;
DROP TABLE IF EXISTS mission_contents CASCADE;
DROP TABLE IF EXISTS missions CASCADE;
DROP TABLE IF EXISTS chapters CASCADE;
DROP TABLE IF EXISTS journeys CASCADE;
DROP TABLE IF EXISTS access_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Drop Liquibase tracking tables
DROP TABLE IF EXISTS databasechangelog CASCADE;
DROP TABLE IF EXISTS databasechangeloglock CASCADE;

-- Drop custom enum types
DROP TYPE IF EXISTS content_type CASCADE;
DROP TYPE IF EXISTS mission_access_level CASCADE;
DROP TYPE IF EXISTS mission_status CASCADE;
DROP TYPE IF EXISTS mission_type CASCADE;
DROP TYPE IF EXISTS reward_type CASCADE;
DROP TYPE IF EXISTS user_role CASCADE;
