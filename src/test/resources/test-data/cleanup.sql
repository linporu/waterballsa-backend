-- Cleanup script for E2E tests
-- This script removes all test data after test execution

-- Delete in order to respect foreign key constraints
DELETE FROM user_mission_progress WHERE TRUE;
DELETE FROM mission_contents WHERE TRUE;
DELETE FROM missions WHERE TRUE;
DELETE FROM chapters WHERE TRUE;
DELETE FROM user_journey_purchases WHERE TRUE;
DELETE FROM journeys WHERE TRUE;
DELETE FROM access_tokens WHERE TRUE;
DELETE FROM users WHERE TRUE;

-- Drop custom types (must be done after deleting data from tables that use them)
DROP TYPE IF EXISTS user_role CASCADE;
DROP TYPE IF EXISTS mission_access_level CASCADE;
DROP TYPE IF EXISTS mission_status CASCADE;
DROP TYPE IF EXISTS mission_type CASCADE;
DROP TYPE IF EXISTS content_type CASCADE;
DROP TYPE IF EXISTS reward_type CASCADE;
