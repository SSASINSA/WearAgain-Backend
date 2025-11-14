package com.ssasinsa.wearagain.domain.ticket.exception;

import com.ssasinsa.wearagain.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum TicketErrorCode implements ErrorCode {
    TICKET_DATA_NOT_FOUND("T2001", "교환 티켓 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()),
    TICKET_BALANCE_EMPTY("T2002", "교환 티켓 잔여 수량이 없습니다.", HttpStatus.CONFLICT.value()),
    TICKET_QR_TOKEN_STORE_FAILED("T2003", "QR 토큰 저장 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());

    private final String code;
    private final String message;
    private final int status;

    TicketErrorCode(String code, String message, int status) {
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
