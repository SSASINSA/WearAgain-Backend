package com.ssasinsa.wearagain.global.storage;

import com.ssasinsa.wearagain.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum ImageStorageErrorCode implements ErrorCode {
    INVALID_FILE("I1001", "이미지 정보가 올바르지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    IO_ERROR("I1002", "이미지 업로드 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());

    private final String code;
    private final String message;
    private final int status;

    ImageStorageErrorCode(String code, String message, int status) {
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
