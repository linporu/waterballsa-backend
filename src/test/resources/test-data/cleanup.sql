-- Cleanup script for E2E tests
-- This script removes all test data after test execution

-- Delete in order to respect foreign key constraints
DELETE FROM user_mission_progress WHERE TRUE;
DELETE FROM order_items WHERE TRUE;
DELETE FROM user_journeys WHERE TRUE;
DELETE FROM orders WHERE TRUE;
DELETE FROM rewards WHERE TRUE;
DELETE FROM mission_contents WHERE TRUE;
DELETE FROM missions WHERE TRUE;
DELETE FROM chapters WHERE TRUE;
DELETE FROM journeys WHERE TRUE;
DELETE FROM access_tokens WHERE TRUE;
DELETE FROM users WHERE TRUE;
