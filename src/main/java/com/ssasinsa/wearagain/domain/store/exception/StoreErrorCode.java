package com.ssasinsa.wearagain.domain.store.exception;

import com.ssasinsa.wearagain.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum StoreErrorCode implements ErrorCode {
    STORE_UNAUTHORIZED("S1000", "스토어 관리자 권한이 필요합니다.", HttpStatus.UNAUTHORIZED.value()),
    STORE_FORBIDDEN("S1001", "스토어 관리자 기능 접근이 거부되었습니다.", HttpStatus.FORBIDDEN.value()),
    STORE_ITEM_NOT_FOUND("S1002", "스토어 상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()),
    STORE_ITEM_STATUS_INVALID("S1003", "스토어 상품 상태 값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    STORE_ITEM_ALREADY_DELETED("S1004", "이미 삭제된 스토어 상품입니다.", HttpStatus.CONFLICT.value()),
    STORE_QUERY_INVALID("S1005", "스토어 상품 조회 요청이 올바르지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    STORE_IMAGE_INVALID("S1006", "스토어 상품 이미지 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    STORE_IMAGE_UPLOAD_FAILED("S1007", "스토어 상품 이미지 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value()),
    STORE_STATUS_UPDATE_FORBIDDEN("S1008", "스토어 상품 상태를 변경할 수 없습니다.", HttpStatus.FORBIDDEN.value()),
    STORE_PICKUP_LOCATION_INVALID("S1009", "유효하지 않은 픽업 장소 요청입니다.", HttpStatus.BAD_REQUEST.value()),
    STORE_ITEM_INACTIVE("S1010", "판매 불가 상태의 상품입니다.", HttpStatus.BAD_REQUEST.value()),
    STORE_STOCK_SHORTAGE("S1011", "재고가 부족합니다.", HttpStatus.CONFLICT.value()),
    STORE_CREDIT_NOT_ENOUGH("S1012", "크레딧이 부족합니다.", HttpStatus.CONFLICT.value()),
    STORE_ORDER_NOT_FOUND("S1013", "주문을 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()),
    STORE_ORDER_CANCEL_INVALID("S1014", "취소할 수 없는 주문 상태입니다.", HttpStatus.CONFLICT.value()),
    STORE_PURCHASE_LIMIT_EXCEEDED("S1015", "사용자별 최대 구매 횟수를 초과했습니다.", HttpStatus.CONFLICT.value()),
    STORE_ORDER_STATUS_INVALID("S1016", "주문 상태 값이 올바르지 않습니다.", HttpStatus.BAD_REQUEST.value());

    private final String code;
    private final String message;
    private final int status;

    StoreErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getStatus() {
        return status;
    }
}
