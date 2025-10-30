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
