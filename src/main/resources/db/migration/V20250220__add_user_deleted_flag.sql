ALTER TABLE users
    ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 AFTER is_suspended;
