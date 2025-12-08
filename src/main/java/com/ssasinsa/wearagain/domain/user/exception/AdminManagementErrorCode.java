package com.ssasinsa.wearagain.domain.user.exception;

import com.ssasinsa.wearagain.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AdminManagementErrorCode implements ErrorCode {
    ADMIN_USER_NOT_FOUND("UA2001", "관리자 계정을 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()),
    DELETE_FORBIDDEN("UA2002", "해당 관리자 계정을 삭제할 수 없습니다.", HttpStatus.FORBIDDEN.value()),
    INVALID_REQUEST("UA2003", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST.value());

    private final String code;
    private final String message;
    private final int status;

    AdminManagementErrorCode(String code, String message, int status) {
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
