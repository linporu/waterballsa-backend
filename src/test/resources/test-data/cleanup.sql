-- Cleanup script for E2E tests
-- This script removes all test data after test execution

-- Delete in order to respect foreign key constraints
DELETE FROM course_enrollments;
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM courses;
DELETE FROM users;

-- Reset sequences
ALTER SEQUENCE IF EXISTS users_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS courses_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS orders_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS order_items_id_seq RESTART WITH 1;
ALTER SEQUENCE IF EXISTS course_enrollments_id_seq RESTART WITH 1;
