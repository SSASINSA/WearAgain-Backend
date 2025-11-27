package com.ssasinsa.wearagain.domain.user.exception;

import com.ssasinsa.wearagain.global.exception.CustomException;

public class UserException extends CustomException {

    public UserException(UserErrorCode errorCode) {
        super(errorCode);
    }

    public UserException(UserErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
