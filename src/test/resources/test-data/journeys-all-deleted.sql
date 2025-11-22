-- Test data for empty journey list scenario
-- All journeys are soft-deleted

-- Journey 1: Soft-deleted
INSERT INTO journeys (id, slug, title, description, cover_image_url, price, teacher_name, created_at, updated_at, deleted_at)
VALUES
    (1, 'deleted-journey-1', 'Deleted Journey 1', 'This should not appear', 'https://example.com/deleted1.jpg', 1999.00, 'Ghost 1', '2024-01-01 10:00:00', '2024-01-01 10:00:00', '2024-01-01 11:00:00');

-- Journey 2: Soft-deleted
INSERT INTO journeys (id, slug, title, description, cover_image_url, price, teacher_name, created_at, updated_at, deleted_at)
VALUES
    (2, 'deleted-journey-2', 'Deleted Journey 2', 'This should not appear', 'https://example.com/deleted2.jpg', 2999.00, 'Ghost 2', '2024-01-02 10:00:00', '2024-01-02 10:00:00', '2024-01-02 11:00:00');
