-- Sample course data for E2E testing
-- This script inserts test courses into the database for testing purposes

INSERT INTO courses (id, title, description, price, instructor, duration_hours, level, category, created_at, updated_at)
VALUES
    (1, 'Java Spring Boot Fundamentals', 'Learn the basics of Spring Boot framework and build RESTful APIs', 99.99, 'John Doe', 20, 'BEGINNER', 'BACKEND', NOW(), NOW()),
    (2, 'Advanced Microservices Architecture', 'Master microservices patterns with Spring Cloud', 149.99, 'Jane Smith', 35, 'ADVANCED', 'BACKEND', NOW(), NOW()),
    (3, 'React for Beginners', 'Build modern web applications with React', 89.99, 'Mike Johnson', 25, 'BEGINNER', 'FRONTEND', NOW(), NOW()),
    (4, 'Docker and Kubernetes Essentials', 'Learn container orchestration and deployment', 129.99, 'Sarah Lee', 30, 'INTERMEDIATE', 'DEVOPS', NOW(), NOW()),
    (5, 'Database Design and Optimization', 'Master SQL and database performance tuning', 109.99, 'David Chen', 28, 'INTERMEDIATE', 'DATABASE', NOW(), NOW());

-- Reset sequence for auto-increment
ALTER SEQUENCE courses_id_seq RESTART WITH 6;
