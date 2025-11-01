package com.ssasinsa.wearagain.domain.auth.exception;

import com.ssasinsa.wearagain.global.exception.CustomException;

public class AdminAuthException extends CustomException {

    public AdminAuthException(AdminAuthErrorCode errorCode) {
        super(errorCode);
    }

    public AdminAuthException(AdminAuthErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
