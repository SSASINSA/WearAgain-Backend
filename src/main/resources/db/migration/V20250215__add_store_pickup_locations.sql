-- 스토어 아이템 픽업 장소 JSON 필드 추가 및 유효성 제약
ALTER TABLE store_items
    ADD COLUMN pickup_locations JSON NOT NULL DEFAULT (JSON_ARRAY()) AFTER stock,
    ADD CONSTRAINT chk_store_items_pickup_locations CHECK (JSON_VALID(pickup_locations) AND JSON_TYPE(pickup_locations) = 'ARRAY');

-- 스토어 주문 픽업 장소 필드 추가
ALTER TABLE store_orders
    ADD COLUMN pickup_location VARCHAR(255) NOT NULL DEFAULT '' AFTER quantity;
