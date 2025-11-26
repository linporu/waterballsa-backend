-- Test data for journey detail API with user status testing
-- This includes journeys, chapters, missions, users, orders, and user_journeys

-- Insert test journeys
INSERT INTO journeys (id, slug, title, description, cover_image_url, price, teacher_name, created_at, updated_at, deleted_at)
VALUES
    (1, 'design-patterns-mastery', '軟體設計模式精通之旅', '用 C.A. 模式大大提昇系統思維能力', 'https://example.com/cover1.jpg', 1999.00, '水球潘', NOW(), NOW(), NULL);

-- Insert test chapters
INSERT INTO chapters (id, journey_id, title, order_index, created_at, updated_at, deleted_at)
VALUES
    (1, 1, '課程介紹', 1, NOW(), NOW(), NULL),
    (2, 1, 'UML 基礎', 2, NOW(), NOW(), NULL);

-- Insert test missions
INSERT INTO missions (id, chapter_id, title, type, description, access_level, order_index, created_at, updated_at, deleted_at)
VALUES
    -- Chapter 1 missions (Public access)
    (1, 1, '這門課手把手帶你成為架構設計的高手', 'VIDEO', '課程介紹', 'PUBLIC', 1, NOW(), NOW(), NULL),
    (2, 1, '你該知道：在 AI 的時代下，只會下 prompt 絕對寫不出好 Code', 'VIDEO', '課程前言', 'PUBLIC', 2, NOW(), NOW(), NULL),
    -- Chapter 2 missions (Purchased access required)
    (3, 2, '架構師該如何思考', 'VIDEO', 'UML 介紹', 'PURCHASED', 1, NOW(), NOW(), NULL);

-- Note: Users, orders, and user_journeys will be created dynamically in tests
-- This ensures password hashing is correct and IDs are properly managed
