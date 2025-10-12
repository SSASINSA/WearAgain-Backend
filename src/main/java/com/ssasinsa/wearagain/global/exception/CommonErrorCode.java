package com.ssasinsa.wearagain.global.exception;

import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {
    INTERNAL_SERVER_ERROR("C1000", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value()),
    INVALID_REQUEST("C1001", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST.value()),
    UNAUTHORIZED("C1002", "인증이 필요합니다.", HttpStatus.UNAUTHORIZED.value()),
    FORBIDDEN("C1003", "접근이 거부되었습니다.", HttpStatus.FORBIDDEN.value());

    private final String code;
    private final String message;
    private final int status;

    CommonErrorCode(String code, String message, int status) {
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
