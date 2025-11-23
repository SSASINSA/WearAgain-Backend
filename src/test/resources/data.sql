INSERT INTO growth_reward_rules (growth_reward_rules_id, level_required, credit_reward, created_at)
VALUES
    (1, 1, 100, NOW()),
    (2, 2, 100, NOW()),
    (3, 3, 100, NOW()),
    (4, 4, 100, NOW()),
    (5, 5, 100, NOW()),
    (6, 6, 100, NOW()),
    (7, 7, 100, NOW()),
    (8, 8, 100, NOW()),
    (9, 9, 100, NOW()),
    (10, 10, 300, NOW());

INSERT INTO users (users_id, email, display_name, profile_image_url, ticket_balance, credit_balance, is_suspended, created_at, updated_at)
VALUES
    (101, 'user101@wearagain.local', '테스트유저101', NULL, 0, 0, FALSE, NOW(), NOW()),
    (102, 'user102@wearagain.local', '테스트유저102', NULL, 0, 0, FALSE, NOW(), NOW()),
    (103, 'user103@wearagain.local', '테스트유저103', NULL, 0, 0, FALSE, NOW(), NOW()),
    (104, 'user104@wearagain.local', '테스트유저104', NULL, 0, 0, FALSE, NOW(), NOW()),
    (105, 'user105@wearagain.local', '테스트유저105', NULL, 0, 0, FALSE, NOW(), NOW()),
    (106, 'user106@wearagain.local', '테스트유저106', NULL, 0, 0, FALSE, NOW(), NOW()),
    (107, 'user107@wearagain.local', '테스트유저107', NULL, 0, 0, FALSE, NOW(), NOW()),
    (108, 'user108@wearagain.local', '테스트유저108', NULL, 0, 0, FALSE, NOW(), NOW()),
    (109, 'user109@wearagain.local', '테스트유저109', NULL, 0, 0, FALSE, NOW(), NOW()),
    (110, 'user110@wearagain.local', '테스트유저110', NULL, 0, 0, FALSE, NOW(), NOW()),
    (111, 'user111@wearagain.local', '테스트유저111', NULL, 0, 0, FALSE, NOW(), NOW()),
    (112, 'user112@wearagain.local', '테스트유저112', NULL, 0, 0, FALSE, NOW(), NOW()),
    (113, 'user113@wearagain.local', '테스트유저113', NULL, 0, 0, FALSE, NOW(), NOW()),
    (114, 'user114@wearagain.local', '테스트유저114', NULL, 0, 0, FALSE, NOW(), NOW()),
    (115, 'user115@wearagain.local', '테스트유저115', NULL, 0, 0, FALSE, NOW(), NOW()),
    (116, 'user116@wearagain.local', '테스트유저116', NULL, 0, 0, FALSE, NOW(), NOW()),
    (117, 'user117@wearagain.local', '테스트유저117', NULL, 0, 0, FALSE, NOW(), NOW()),
    (118, 'user118@wearagain.local', '테스트유저118', NULL, 0, 0, FALSE, NOW(), NOW()),
    (119, 'user119@wearagain.local', '테스트유저119', NULL, 0, 0, FALSE, NOW(), NOW()),
    (120, 'user120@wearagain.local', '테스트유저120', NULL, 0, 0, FALSE, NOW(), NOW());

INSERT INTO user_growths (user_growths_id, users_id, current_level, exp, repair_count, cycles, magic_scissor_count, total_scissor_used, created_at, updated_at)
VALUES
    (101, 101, 1, 0, 12, 0, 0, 12, NOW(), NOW()),
    (102, 102, 1, 0, 45, 0, 0, 45, NOW(), NOW()),
    (103, 103, 1, 0, 7, 0, 0, 7, NOW(), NOW()),
    (104, 104, 1, 0, 63, 0, 0, 63, NOW(), NOW()),
    (105, 105, 1, 0, 28, 0, 0, 28, NOW(), NOW()),
    (106, 106, 1, 0, 91, 0, 0, 91, NOW(), NOW()),
    (107, 107, 1, 0, 39, 0, 0, 39, NOW(), NOW()),
    (108, 108, 1, 0, 54, 0, 0, 54, NOW(), NOW()),
    (109, 109, 1, 0, 5, 0, 0, 5, NOW(), NOW()),
    (110, 110, 1, 0, 76, 0, 0, 76, NOW(), NOW()),
    (111, 111, 1, 0, 18, 0, 0, 18, NOW(), NOW()),
    (112, 112, 1, 0, 32, 0, 0, 32, NOW(), NOW()),
    (113, 113, 1, 0, 99, 0, 0, 99, NOW(), NOW()),
    (114, 114, 1, 0, 21, 0, 0, 21, NOW(), NOW()),
    (115, 115, 1, 0, 66, 0, 0, 66, NOW(), NOW()),
    (116, 116, 1, 0, 14, 0, 0, 14, NOW(), NOW()),
    (117, 117, 1, 0, 47, 0, 0, 47, NOW(), NOW()),
    (118, 118, 1, 0, 3, 0, 0, 3, NOW(), NOW()),
    (119, 119, 1, 0, 85, 0, 0, 85, NOW(), NOW()),
    (120, 120, 1, 0, 58, 0, 0, 58, NOW(), NOW());

INSERT INTO magic_scissor_histories (magic_scissor_histories_id, users_id, user_growths_id, delta, reason, created_at, updated_at)
VALUES
    (1001, 101, 101, -1, 'USED_REPAIR', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY),
    (1002, 102, 102, -5, 'USED_REPAIR', NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY),
    (1003, 103, 103, -3, 'USED_REPAIR', NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY),
    (1004, 104, 104, -7, 'USED_REPAIR', NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
    (1005, 105, 105, -4, 'USED_REPAIR', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
    (1006, 106, 106, -9, 'USED_REPAIR', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
    (1007, 107, 107, -6, 'USED_REPAIR', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
    (1008, 108, 108, -8, 'USED_REPAIR', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
    (1009, 109, 109, -2, 'USED_REPAIR', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
    (1010, 110, 110, -10, 'USED_REPAIR', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
    (1011, 111, 111, -1, 'USED_REPAIR', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY),
    (1012, 112, 112, -1, 'USED_REPAIR', NOW() - INTERVAL 2 DAY, NOW() - INTERVAL 2 DAY),
    (1013, 113, 113, -1, 'USED_REPAIR', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY),
    (1014, 114, 114, -1, 'USED_REPAIR', NOW() - INTERVAL 4 DAY, NOW() - INTERVAL 4 DAY),
    (1015, 115, 115, -1, 'USED_REPAIR', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY),
    (1016, 116, 116, -1, 'USED_REPAIR', NOW() - INTERVAL 6 DAY, NOW() - INTERVAL 6 DAY),
    (1017, 117, 117, -1, 'USED_REPAIR', NOW() - INTERVAL 7 DAY, NOW() - INTERVAL 7 DAY),
    (1018, 118, 118, -1, 'USED_REPAIR', NOW() - INTERVAL 8 DAY, NOW() - INTERVAL 8 DAY),
    (1019, 119, 119, -1, 'USED_REPAIR', NOW() - INTERVAL 9 DAY, NOW() - INTERVAL 9 DAY),
    (1020, 120, 120, -1, 'USED_REPAIR', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY);

INSERT INTO store_items (store_items_id, name, description, category, price, stock, status, created_at, updated_at)
VALUES
    (301, '업사이클 에코백', '재활용 원단으로 제작한 친환경 에코백입니다.', 'bag', 12000, 100, 'ACTIVE', NOW(), NOW()),
    (302, '리사이클 텀블러', '이중 진공 구조로 보온 보냉이 가능한 스테인리스 텀블러입니다.', 'kitchen', 18000, 50, 'ACTIVE', NOW(), NOW()),
    (303, '리유저블 컵 세트', '가벼운 소재의 재사용 컵 4P 세트입니다.', 'kitchen', 9000, 0, 'INACTIVE', NOW(), NOW()),
    (304, '리사이클 토트백', 'md-docs의 download.png를 활용한 기본 토트백 샘플입니다.', 'bag', 15000, 40, 'ACTIVE', NOW(), NOW()),
    (305, '리유저블 워터보틀', 'md-docs의 download (1).png를 활용한 워터보틀 샘플입니다.', 'kitchen', 11000, 80, 'ACTIVE', NOW(), NOW()),
    (306, '업사이클 파우치', 'md-docs의 download (2).png를 활용한 파우치 샘플입니다.', 'accessory', 8000, 25, 'ACTIVE', NOW(), NOW());

INSERT INTO store_item_images (store_item_images_id, store_items_id, image_url, sort_order, created_at, updated_at)
VALUES
    (501, 301, 'https://cdn.wearagain.local/store/items/301/main.jpg', 1, NOW(), NOW()),
    (502, 301, 'https://cdn.wearagain.local/store/items/301/detail-1.jpg', 2, NOW(), NOW()),
    (503, 302, 'https://cdn.wearagain.local/store/items/302/main.jpg', 1, NOW(), NOW()),
    (504, 303, 'https://cdn.wearagain.local/store/items/303/main.jpg', 1, NOW(), NOW()),
    (505, 304, '/upload/store/md-docs/download.png', 1, NOW(), NOW()),
    (506, 305, '/upload/store/md-docs/download (1).png', 1, NOW(), NOW()),
    (507, 306, '/upload/store/md-docs/download (2).png', 1, NOW(), NOW());
