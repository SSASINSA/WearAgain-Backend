-- 행사 옵션 삭제 시 연결된 신청까지 함께 제거되도록 FK를 재정의한다.
ALTER TABLE event_applications
    DROP FOREIGN KEY FKmcvrh0u6bxk36ka5lrsw3n1rw;

ALTER TABLE event_applications
    ADD CONSTRAINT FKmcvrh0u6bxk36ka5lrsw3n1rw
        FOREIGN KEY (event_option_id) REFERENCES event_option (event_option_id)
        ON DELETE CASCADE;
