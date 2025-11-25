-- Order test data with expired order for E2E testing
-- This script includes test journeys but does NOT insert an expired order
-- The expired order will be created and manipulated directly in the test code

-- Insert test journeys
INSERT INTO journeys (id, slug, title, description, cover_image_url, price, teacher_name, created_at, updated_at, deleted_at)
VALUES
    (1, 'design-patterns-mastery', '軟體設計模式精通之旅', '用 C.A. 模式大大提昇系統思維能力', 'https://example.com/cover1.jpg', 1999.00, '水球潘', NOW(), NOW(), NULL),
    (2, 'spring-boot-complete', 'Spring Boot 完全攻略', '從零開始學習 Spring Boot 開發', 'https://example.com/cover2.jpg', 2999.00, 'John Doe', NOW(), NOW(), NULL);
