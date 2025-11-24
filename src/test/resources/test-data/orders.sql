-- Order test data for E2E testing
-- This script inserts test journeys for order creation, payment and purchase flow testing

-- Insert test journeys with different prices
-- Journey 1: Affordable course
INSERT INTO journeys (id, slug, title, description, cover_image_url, price, teacher_name, created_at, updated_at, deleted_at)
VALUES
    (1, 'design-patterns-mastery', '軟體設計模式精通之旅', '用 C.A. 模式大大提昇系統思維能力', 'https://example.com/cover1.jpg', 1999.00, '水球潘', NOW(), NOW(), NULL);

-- Journey 2: Mid-range course
INSERT INTO journeys (id, slug, title, description, cover_image_url, price, teacher_name, created_at, updated_at, deleted_at)
VALUES
    (2, 'spring-boot-complete', 'Spring Boot 完全攻略', '從零開始學習 Spring Boot 開發', 'https://example.com/cover2.jpg', 2999.00, 'John Doe', NOW(), NOW(), NULL);

-- Journey 3: Premium course for price lock testing
INSERT INTO journeys (id, slug, title, description, cover_image_url, price, teacher_name, created_at, updated_at, deleted_at)
VALUES
    (3, 'ai-bdd-course', 'AI x BDD：規格驅動全自動開發術', 'AI 輔助 BDD 開發完整課程', 'https://example.com/cover3.jpg', 7599.00, '水球潘', NOW(), NOW(), NULL);
