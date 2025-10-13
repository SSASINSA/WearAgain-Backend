package com.ssasinsa.wearagain.global.exception;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record ErrorResponse(
        String timestamp,
        int statusCode,
        String errorCode,
        String message,
        String path
) {

    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC).toString(),
                errorCode.getStatus(),
                errorCode.getCode(),
                message,
                path
        );
    }
}
