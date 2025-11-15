package com.ssasinsa.wearagain.domain.ticket.exception;

import com.ssasinsa.wearagain.global.exception.CustomException;

public class TicketException extends CustomException {

    public TicketException(TicketErrorCode errorCode) {
        super(errorCode);
    }

    public TicketException(TicketErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
