-- Journey list test data for E2E testing
-- This script inserts test journeys for journey list API testing

-- Insert test journeys
-- Journey 1: Created first (older)
INSERT INTO journeys (id, slug, title, description, cover_image_url, price, teacher_name, created_at, updated_at, deleted_at)
VALUES
    (1, 'design-patterns-mastery', '軟體設計模式精通之旅', '用 C.A. 模式大大提昇系統思維能力', 'https://example.com/cover1.jpg', 1999.00, '水球潘', '2024-01-01 10:00:00', '2024-01-01 10:00:00', NULL);

-- Journey 2: Created second (newer)
INSERT INTO journeys (id, slug, title, description, cover_image_url, price, teacher_name, created_at, updated_at, deleted_at)
VALUES
    (2, 'spring-boot-complete', 'Spring Boot 完全攻略', '從零開始學習 Spring Boot 開發', 'https://example.com/cover2.jpg', 2999.00, 'John Doe', '2024-01-02 10:00:00', '2024-01-02 10:00:00', NULL);

-- Journey 999: Soft-deleted journey (should not appear in list)
INSERT INTO journeys (id, slug, title, description, cover_image_url, price, teacher_name, created_at, updated_at, deleted_at)
VALUES
    (999, 'deleted-journey', 'Deleted Journey', 'This should not appear', 'https://example.com/deleted.jpg', 999.00, 'Ghost', '2024-01-03 10:00:00', '2024-01-03 10:00:00', '2024-01-03 11:00:00');
