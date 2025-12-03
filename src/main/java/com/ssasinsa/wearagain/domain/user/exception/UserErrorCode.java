package com.ssasinsa.wearagain.domain.user.exception;

import com.ssasinsa.wearagain.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND("U1001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()),
    INVALID_REQUEST("U1002", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST.value()),
    FEATURE_NOT_AVAILABLE("U1003", "지원하지 않는 기능입니다.", HttpStatus.FORBIDDEN.value()),
    USER_ALREADY_WITHDRAWN("U1004", "이미 탈퇴 처리된 사용자입니다.", HttpStatus.BAD_REQUEST.value());

    private final String code;
    private final String message;
    private final int status;

    UserErrorCode(String code, String message, int status) {
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
