-- Sample mission data for E2E testing
-- This script inserts test journeys, chapters, missions, and mission contents

-- Insert test journeys
INSERT INTO journeys (id, slug, title, description, cover_image_url, price, teacher_name, created_at, updated_at, deleted_at)
VALUES
    (1, 'design-patterns-mastery', '軟體設計模式精通之旅', '用 C.A. 模式大大提昇系統思維能力', 'https://example.com/cover1.jpg', 1999.00, '水球潘', NOW(), NOW(), NULL),
    (2, 'spring-boot-complete', 'Spring Boot 完全攻略', '從零開始學習 Spring Boot 開發', 'https://example.com/cover2.jpg', 2999.00, 'John Doe', NOW(), NOW(), NULL),
    (999, 'deleted-journey', 'Deleted Journey', 'This should not appear', 'https://example.com/deleted.jpg', 999.00, 'Ghost', NOW(), NOW(), NOW());

-- Insert test chapters
INSERT INTO chapters (id, journey_id, title, order_index, created_at, updated_at, deleted_at)
VALUES
    -- Journey 1 chapters
    (1, 1, '課程介紹', 1, NOW(), NOW(), NULL),
    (2, 1, 'UML 基礎', 2, NOW(), NOW(), NULL),
    (3, 1, '設計模式入門', 3, NOW(), NOW(), NULL),
    -- Journey 2 chapters
    (4, 2, 'Spring Boot 簡介', 1, NOW(), NOW(), NULL),
    -- Deleted chapter
    (999, 1, 'Deleted Chapter', 999, NOW(), NOW(), NOW());

-- Insert test missions
INSERT INTO missions (id, chapter_id, title, type, description, access_level, order_index, created_at, updated_at, deleted_at)
VALUES
    -- Chapter 1 missions (Free preview missions)
    (1, 1, '課程介紹：這門課手把手帶你成為架構設計的高手', 'VIDEO', '思維升級：Christopher Alexander 模式六大要素及模式語言', 'PUBLIC', 1, NOW(), NOW(), NULL),
    (2, 1, '課程大綱與學習地圖', 'VIDEO', '完整的課程結構介紹', 'PUBLIC', 2, NOW(), NOW(), NULL),

    -- Chapter 2 missions (Purchased access required)
    (3, 2, 'UML 不是英文縮寫字', 'ARTICLE', 'UML 統一塑模語言完整介紹', 'PURCHASED', 1, NOW(), NOW(), NULL),
    (4, 2, 'UML 類別圖教學', 'VIDEO', '深入理解 UML 類別圖的繪製方法', 'PURCHASED', 2, NOW(), NOW(), NULL),

    -- Chapter 3 missions
    (5, 3, '設計模式概論', 'VIDEO', '23 種設計模式總覽', 'PURCHASED', 1, NOW(), NOW(), NULL),
    (6, 3, '課程回饋問卷', 'QUESTIONNAIRE', '請填寫課程回饋問卷，幫助我們改進課程內容', 'PURCHASED', 2, NOW(), NOW(), NULL),

    -- Chapter 4 missions (Journey 2)
    (7, 4, 'Spring Boot 介紹', 'VIDEO', 'Spring Boot 框架基礎', 'AUTHENTICATED', 1, NOW(), NOW(), NULL),

    -- Deleted mission
    (999, 1, 'Deleted Mission', 'VIDEO', 'This should not appear', 'PUBLIC', 999, NOW(), NOW(), NOW());

-- Insert test mission contents
INSERT INTO mission_contents (id, mission_id, content_type, content_url, content_order, duration_seconds, created_at, updated_at, deleted_at)
VALUES
    -- Mission 1 content (VIDEO)
    (1, 1, 'VIDEO', 'https://cdn.waterballsa.tw/software-design-pattern/videos/c8m1-0/c8m1-0.m3u8', 1, 256, NOW(), NOW(), NULL),

    -- Mission 2 content (VIDEO)
    (2, 2, 'VIDEO', 'https://cdn.waterballsa.tw/intro/outline.m3u8', 1, 180, NOW(), NOW(), NULL),

    -- Mission 3 content (ARTICLE)
    (3, 3, 'ARTICLE', 'https://cdn.waterballsa.tw/articles/uml-intro.html', 1, NULL, NOW(), NOW(), NULL),

    -- Mission 4 content (VIDEO)
    (4, 4, 'VIDEO', 'https://cdn.waterballsa.tw/uml/class-diagram.m3u8', 1, 420, NOW(), NOW(), NULL),

    -- Mission 5 content (VIDEO)
    (5, 5, 'VIDEO', 'https://cdn.waterballsa.tw/patterns/intro.m3u8', 1, 600, NOW(), NOW(), NULL),

    -- Mission 6 content (FORM)
    (6, 6, 'FORM', 'https://forms.waterballsa.tw/feedback-form-1', 1, NULL, NOW(), NOW(), NULL),

    -- Mission 7 content (VIDEO)
    (7, 7, 'VIDEO', 'https://cdn.waterballsa.tw/spring/intro.m3u8', 1, 300, NOW(), NOW(), NULL);

-- Insert test rewards
INSERT INTO rewards (id, mission_id, reward_type, reward_value, created_at, updated_at, deleted_at)
VALUES
    (1, 1, 'EXPERIENCE', 100, NOW(), NOW(), NULL),
    (2, 2, 'EXPERIENCE', 100, NOW(), NOW(), NULL),
    (3, 3, 'EXPERIENCE', 100, NOW(), NOW(), NULL),
    (4, 4, 'EXPERIENCE', 100, NOW(), NOW(), NULL),
    (5, 5, 'EXPERIENCE', 100, NOW(), NOW(), NULL),
    (6, 6, 'EXPERIENCE', 100, NOW(), NOW(), NULL),
    (7, 7, 'EXPERIENCE', 100, NOW(), NOW(), NULL);
