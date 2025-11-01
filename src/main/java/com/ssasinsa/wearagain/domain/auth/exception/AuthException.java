package com.ssasinsa.wearagain.auth.exception;

import com.ssasinsa.wearagain.global.exception.CustomException;
import com.ssasinsa.wearagain.global.exception.ErrorCode;

public class AuthException extends CustomException {

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
