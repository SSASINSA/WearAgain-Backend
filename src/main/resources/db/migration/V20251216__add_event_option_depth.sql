-- 이벤트 옵션 깊이(optionDepth) 관리 컬럼 추가
ALTER TABLE event
    ADD COLUMN option_depth TINYINT NOT NULL DEFAULT 1 COMMENT '옵션 최대 깊이 (1~3)';

-- 기존 이벤트별 실제 옵션 깊이를 계산해 반영 (최대 3단계)
WITH RECURSIVE option_hierarchy AS (
    SELECT eo.event_option_id,
           eo.event_id,
           1 AS depth
    FROM event_option eo
    WHERE eo.parent_event_option_id IS NULL
    UNION ALL
    SELECT child.event_option_id,
           child.event_id,
           parent.depth + 1 AS depth
    FROM event_option child
             INNER JOIN option_hierarchy parent ON child.parent_event_option_id = parent.event_option_id
)
UPDATE event e
    INNER JOIN (
        SELECT event_id,
               LEAST(MAX(depth), 3) AS calculated_depth
        FROM option_hierarchy
        GROUP BY event_id
    ) depth_info ON depth_info.event_id = e.event_id
SET e.option_depth = depth_info.calculated_depth;

-- 옵션 데이터가 없는 이벤트는 기본값 1 유지
UPDATE event
SET option_depth = 1
WHERE option_depth IS NULL;
