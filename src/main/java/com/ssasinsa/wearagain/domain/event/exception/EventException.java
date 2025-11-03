package com.ssasinsa.wearagain.domain.event.exception;

import com.ssasinsa.wearagain.global.exception.CustomException;

public class EventException extends CustomException {

    public EventException(EventErrorCode errorCode) {
        super(errorCode);
    }

    public EventException(EventErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
