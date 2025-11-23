package com.ssasinsa.wearagain.domain.store.exception;

import com.ssasinsa.wearagain.global.exception.CustomException;

public class StoreException extends CustomException {

    public StoreException(StoreErrorCode errorCode) {
        super(errorCode);
    }

    public StoreException(StoreErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
