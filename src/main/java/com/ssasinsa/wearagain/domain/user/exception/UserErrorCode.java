package com.ssasinsa.wearagain.domain.user.exception;

import com.ssasinsa.wearagain.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND("U1001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()),
    INVALID_REQUEST("U1002", "잘못된 요청입니다.", HttpStatus.BAD_REQUEST.value()),
    FEATURE_NOT_AVAILABLE("U1003", "지원하지 않는 기능입니다.", HttpStatus.FORBIDDEN.value());

    private final String code;
    private final String message;
    private final int status;
}
