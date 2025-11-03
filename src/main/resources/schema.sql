-- 사용자 도메인 스키마 DDL
DROP TABLE IF EXISTS user_oauth_accounts;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    users_id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    profile_image_url VARCHAR(512),
    ticket_balance INT NOT NULL DEFAULT 0,
    credit_balance INT NOT NULL DEFAULT 0,
    is_suspended BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_users PRIMARY KEY (users_id),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE user_oauth_accounts (
    user_oauth_accounts_id BIGINT NOT NULL AUTO_INCREMENT,
    provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(64) NOT NULL,
    email VARCHAR(255) NOT NULL,
    users_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_user_oauth_accounts PRIMARY KEY (user_oauth_accounts_id),
    CONSTRAINT uk_user_oauth_accounts_provider_user UNIQUE (provider, provider_user_id),
    CONSTRAINT fk_user_oauth_accounts_user FOREIGN KEY (users_id) REFERENCES users (users_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

-- OAuth 계정 조회 시 사용자 기준 탐색 속도 향상 인덱스
CREATE INDEX idx_user_oauth_accounts_user_id
    ON user_oauth_accounts (users_id);

-- ===========================================================
-- 🚨 Additional Domain Reset (drop in dependency order)
-- ===========================================================

DROP TABLE IF EXISTS ticket_histories;
DROP TABLE IF EXISTS credit_histories;
DROP TABLE IF EXISTS impact_analytics;
DROP TABLE IF EXISTS store_orders;
DROP TABLE IF EXISTS store_item_images;
DROP TABLE IF EXISTS store_items;
DROP TABLE IF EXISTS event_applications;
DROP TABLE IF EXISTS event_option;
DROP TABLE IF EXISTS event_image;
DROP TABLE IF EXISTS event;
DROP TABLE IF EXISTS likes;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS community_post_images;
DROP TABLE IF EXISTS community_posts;
DROP TABLE IF EXISTS community_categories;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS user_mascots;
DROP TABLE IF EXISTS mascot_reward_rules;
DROP TABLE IF EXISTS admin_signup_requests;
DROP TABLE IF EXISTS admin_users;

-- ===========================================================
-- 🧑‍💼 Admin Domain
-- ===========================================================

CREATE TABLE admin_users (
    admin_users_id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_admin_users PRIMARY KEY (admin_users_id),
    CONSTRAINT uk_admin_users_email UNIQUE (email)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE admin_signup_requests (
    admin_signup_requests_id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    requested_role VARCHAR(20) NOT NULL,
    reason VARCHAR(500),
    rejection_reason VARCHAR(500),
    status VARCHAR(20) NOT NULL,
    reviewed_by BIGINT,
    reviewed_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_admin_signup_requests PRIMARY KEY (admin_signup_requests_id),
    CONSTRAINT fk_admin_signup_requests_reviewer FOREIGN KEY (reviewed_by) REFERENCES admin_users (admin_users_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_admin_signup_requests_status ON admin_signup_requests (status);
CREATE INDEX idx_admin_signup_requests_email ON admin_signup_requests (email);

-- ===========================================================
-- 🎪 Event Domain
-- ===========================================================

CREATE TABLE event (
    event_id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_event PRIMARY KEY (event_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_event_status ON event (status);

CREATE TABLE event_image (
    event_image_id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    url VARCHAR(1024) NOT NULL,
    alt_text VARCHAR(255),
    display_order INT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_event_image PRIMARY KEY (event_image_id),
    CONSTRAINT fk_event_image_event FOREIGN KEY (event_id) REFERENCES event (event_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_event_image_event ON event_image (event_id);

CREATE TABLE event_option (
    event_option_id BIGINT NOT NULL AUTO_INCREMENT,
    event_id BIGINT NOT NULL,
    parent_event_option_id BIGINT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    display_order INT NOT NULL,
    capacity INT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_event_option PRIMARY KEY (event_option_id),
    CONSTRAINT fk_event_option_event FOREIGN KEY (event_id) REFERENCES event (event_id),
    CONSTRAINT fk_event_option_parent FOREIGN KEY (parent_event_option_id) REFERENCES event_option (event_option_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_event_option_event ON event_option (event_id);
CREATE INDEX idx_event_option_parent ON event_option (parent_event_option_id);

CREATE TABLE event_applications (
    event_applications_id BIGINT NOT NULL AUTO_INCREMENT,
    users_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    event_option_id BIGINT NOT NULL,
    status ENUM('APPLIED','CANCELED','REJECTED','CHECKED_IN') NOT NULL DEFAULT 'APPLIED',
    reason VARCHAR(255),
    qr_token VARCHAR(64),
    applied_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    canceled_at DATETIME(6),
    rejected_at DATETIME(6),
    checked_in_at DATETIME(6),
    CONSTRAINT pk_event_applications PRIMARY KEY (event_applications_id),
    CONSTRAINT fk_event_applications_user FOREIGN KEY (users_id) REFERENCES users (users_id),
    CONSTRAINT fk_event_applications_event FOREIGN KEY (event_id) REFERENCES event (event_id),
    CONSTRAINT fk_event_applications_option FOREIGN KEY (event_option_id) REFERENCES event_option (event_option_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_event_applications_user ON event_applications (users_id);
CREATE INDEX idx_event_applications_event ON event_applications (event_id);
CREATE INDEX idx_event_applications_option ON event_applications (event_option_id);
CREATE INDEX idx_event_applications_status ON event_applications (status);

-- ===========================================================
-- 🏪 Store Domain
-- ===========================================================

CREATE TABLE store_items (
    store_items_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    price INT NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    status ENUM('ACTIVE','INACTIVE','DELETED') NOT NULL DEFAULT 'ACTIVE',
    deleted_at DATETIME(6),
    deleted_by BIGINT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_store_items PRIMARY KEY (store_items_id),
    CONSTRAINT fk_store_items_deleted_by FOREIGN KEY (deleted_by) REFERENCES admin_users (admin_users_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_store_items_status ON store_items (status);
CREATE INDEX idx_store_items_deleted_by ON store_items (deleted_by);

CREATE TABLE store_item_images (
    store_item_images_id BIGINT NOT NULL AUTO_INCREMENT,
    store_items_id BIGINT NOT NULL,
    image_url VARCHAR(512) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_store_item_images PRIMARY KEY (store_item_images_id),
    CONSTRAINT fk_store_item_images_item FOREIGN KEY (store_items_id) REFERENCES store_items (store_items_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_store_item_images_item ON store_item_images (store_items_id);

CREATE TABLE store_orders (
    store_orders_id BIGINT NOT NULL AUTO_INCREMENT,
    users_id BIGINT NOT NULL,
    store_items_id BIGINT NOT NULL,
    status ENUM('PURCHASED','CANCELED','FAILED') NOT NULL DEFAULT 'PURCHASED',
    price INT NOT NULL,
    refunded_amount INT NOT NULL DEFAULT 0,
    purchased_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    canceled_at DATETIME(6),
    CONSTRAINT pk_store_orders PRIMARY KEY (store_orders_id),
    CONSTRAINT fk_store_orders_user FOREIGN KEY (users_id) REFERENCES users (users_id),
    CONSTRAINT fk_store_orders_item FOREIGN KEY (store_items_id) REFERENCES store_items (store_items_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_store_orders_user ON store_orders (users_id);
CREATE INDEX idx_store_orders_item ON store_orders (store_items_id);
CREATE INDEX idx_store_orders_status ON store_orders (status);

-- ===========================================================
-- 💰 Credit / Ticket / Impact Domain
-- ===========================================================

CREATE TABLE credit_histories (
    credit_histories_id BIGINT NOT NULL AUTO_INCREMENT,
    users_id BIGINT NOT NULL,
    change_amount INT NOT NULL,
    reason VARCHAR(255),
    related_store_orders_id BIGINT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_credit_histories PRIMARY KEY (credit_histories_id),
    CONSTRAINT fk_credit_histories_user FOREIGN KEY (users_id) REFERENCES users (users_id),
    CONSTRAINT fk_credit_histories_order FOREIGN KEY (related_store_orders_id) REFERENCES store_orders (store_orders_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_credit_histories_user ON credit_histories (users_id);
CREATE INDEX idx_credit_histories_order ON credit_histories (related_store_orders_id);

CREATE TABLE ticket_histories (
    ticket_histories_id BIGINT NOT NULL AUTO_INCREMENT,
    users_id BIGINT NOT NULL,
    change_amount INT NOT NULL,
    reason VARCHAR(255),
    related_events_id BIGINT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_ticket_histories PRIMARY KEY (ticket_histories_id),
    CONSTRAINT fk_ticket_histories_user FOREIGN KEY (users_id) REFERENCES users (users_id),
    CONSTRAINT fk_ticket_histories_event FOREIGN KEY (related_events_id) REFERENCES events (events_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_ticket_histories_user ON ticket_histories (users_id);
CREATE INDEX idx_ticket_histories_event ON ticket_histories (related_events_id);

CREATE TABLE impact_analytics (
    impact_analytics_id BIGINT NOT NULL AUTO_INCREMENT,
    users_id BIGINT NOT NULL,
    events_id BIGINT NOT NULL,
    co2_saved DECIMAL(10,2),
    water_saved DECIMAL(10,2),
    energy_saved DECIMAL(10,2),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_impact_analytics PRIMARY KEY (impact_analytics_id),
    CONSTRAINT fk_impact_analytics_user FOREIGN KEY (users_id) REFERENCES users (users_id),
    CONSTRAINT fk_impact_analytics_event FOREIGN KEY (events_id) REFERENCES events (events_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_impact_analytics_user_event ON impact_analytics (users_id, events_id);

-- ===========================================================
-- 🧵 Mascot Domain
-- ===========================================================

CREATE TABLE mascot_reward_rules (
    mascot_reward_rules_id BIGINT NOT NULL AUTO_INCREMENT,
    level_required INT NOT NULL,
    credit_reward INT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_mascot_reward_rules PRIMARY KEY (mascot_reward_rules_id),
    CONSTRAINT uk_mascot_reward_rules_level UNIQUE (level_required)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE user_mascots (
    user_mascots_id BIGINT NOT NULL AUTO_INCREMENT,
    users_id BIGINT NOT NULL,
    current_level INT NOT NULL DEFAULT 1,
    exp INT NOT NULL DEFAULT 0,
    repair_count INT NOT NULL DEFAULT 0,
    cycles INT NOT NULL DEFAULT 0,
    last_rewarded_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_user_mascots PRIMARY KEY (user_mascots_id),
    CONSTRAINT fk_user_mascots_user FOREIGN KEY (users_id) REFERENCES users (users_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE UNIQUE INDEX uk_user_mascots_user ON user_mascots (users_id);

-- ===========================================================
-- 💬 Community Domain
-- ===========================================================

CREATE TABLE community_categories (
    community_categories_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_community_categories PRIMARY KEY (community_categories_id),
    CONSTRAINT uk_community_categories_name UNIQUE (name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE community_posts (
    community_posts_id BIGINT NOT NULL AUTO_INCREMENT,
    users_id BIGINT NOT NULL,
    community_categories_id BIGINT NOT NULL,
    title VARCHAR(255),
    content TEXT NOT NULL,
    like_count INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_community_posts PRIMARY KEY (community_posts_id),
    CONSTRAINT fk_community_posts_user FOREIGN KEY (users_id) REFERENCES users (users_id),
    CONSTRAINT fk_community_posts_category FOREIGN KEY (community_categories_id) REFERENCES community_categories (community_categories_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_community_posts_user ON community_posts (users_id);
CREATE INDEX idx_community_posts_active ON community_posts (is_active, created_at);
CREATE INDEX idx_community_posts_category ON community_posts (community_categories_id);

CREATE TABLE community_post_images (
    community_post_images_id BIGINT NOT NULL AUTO_INCREMENT,
    community_posts_id BIGINT NOT NULL,
    image_url VARCHAR(512) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_community_post_images PRIMARY KEY (community_post_images_id),
    CONSTRAINT fk_community_post_images_post FOREIGN KEY (community_posts_id) REFERENCES community_posts (community_posts_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_community_post_images_post ON community_post_images (community_posts_id);

CREATE TABLE likes (
    likes_id BIGINT NOT NULL AUTO_INCREMENT,
    community_posts_id BIGINT NOT NULL,
    users_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_likes PRIMARY KEY (likes_id),
    CONSTRAINT uk_likes_unique UNIQUE (community_posts_id, users_id),
    CONSTRAINT fk_likes_post FOREIGN KEY (community_posts_id) REFERENCES community_posts (community_posts_id),
    CONSTRAINT fk_likes_user FOREIGN KEY (users_id) REFERENCES users (users_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE comments (
    comments_id BIGINT NOT NULL AUTO_INCREMENT,
    community_posts_id BIGINT NOT NULL,
    users_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_comments PRIMARY KEY (comments_id),
    CONSTRAINT fk_comments_post FOREIGN KEY (community_posts_id) REFERENCES community_posts (community_posts_id),
    CONSTRAINT fk_comments_user FOREIGN KEY (users_id) REFERENCES users (users_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_comments_post ON comments (community_posts_id);

CREATE TABLE reports (
    reports_id BIGINT NOT NULL AUTO_INCREMENT,
    reporter_users_id BIGINT NOT NULL,
    target_type VARCHAR(20),
    target_id BIGINT,
    reason VARCHAR(255),
    status ENUM('PENDING','REVIEWED','REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_reports PRIMARY KEY (reports_id),
    CONSTRAINT fk_reports_user FOREIGN KEY (reporter_users_id) REFERENCES users (users_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_reports_status ON reports (status);
CREATE INDEX idx_reports_target ON reports (target_type, target_id);

-- ===========================================================
-- 🔔 Notification Domain
-- ===========================================================

CREATE TABLE notifications (
    notifications_id BIGINT NOT NULL AUTO_INCREMENT,
    users_id BIGINT NOT NULL,
    type VARCHAR(50),
    content TEXT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT pk_notifications PRIMARY KEY (notifications_id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (users_id) REFERENCES users (users_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE INDEX idx_notifications_user_read ON notifications (users_id, is_read);
