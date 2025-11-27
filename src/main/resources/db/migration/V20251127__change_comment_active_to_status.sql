-- 댓글 is_active 컬럼을 status enum으로 변경
ALTER TABLE comments
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' AFTER content;

-- 기존 데이터 마이그레이션 (is_active = true -> ACTIVE, is_active = false -> INACTIVE)
UPDATE comments
SET status = CASE
    WHEN is_active = 1 THEN 'ACTIVE'
    WHEN is_active = 0 THEN 'INACTIVE'
    ELSE 'ACTIVE'
END;

-- is_active 컬럼 제거
ALTER TABLE comments
    DROP COLUMN is_active;

