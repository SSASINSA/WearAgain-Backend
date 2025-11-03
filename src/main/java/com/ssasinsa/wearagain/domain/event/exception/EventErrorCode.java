package com.ssasinsa.wearagain.domain.event.exception;

import com.ssasinsa.wearagain.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum EventErrorCode implements ErrorCode {
    MISSING_REQUIRED_VALUE("E1001", "필수 입력 값이 누락되었습니다.", HttpStatus.BAD_REQUEST.value()),
    INVALID_EVENT_PERIOD("E1002", "행사 기간이 유효하지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    OPTION_DEPTH_LIMIT_EXCEEDED("E1003", "옵션 트리 깊이가 허용 범위를 초과했습니다.", HttpStatus.BAD_REQUEST.value()),
    INVALID_OPTION_STRUCTURE("E1004", "옵션 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    INVALID_IMAGE_INFORMATION("E1005", "이미지 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    DUPLICATE_OPTION("E1006", "중복된 옵션이 존재합니다.", HttpStatus.CONFLICT.value()),
    EVENT_REGISTRATION_FORBIDDEN("E1007", "행사 등록 권한이 없습니다.", HttpStatus.FORBIDDEN.value()),
    EVENT_REGISTRATION_FAILED("E1008", "행사 등록 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value()),
    IMAGE_UPLOAD_FAILED("E1009", "이미지 업로드 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());

    private final String code;
    private final String message;
    private final int status;

    EventErrorCode(String code, String message, int status) {
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
