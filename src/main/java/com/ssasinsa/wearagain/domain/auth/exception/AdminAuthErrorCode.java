package com.ssasinsa.wearagain.domain.auth.exception;

import com.ssasinsa.wearagain.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AdminAuthErrorCode implements ErrorCode {
    INVALID_INPUT("AD1001", "입력 값이 유효하지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    INVALID_CREDENTIAL("AD1002", "이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED.value()),
    ACCOUNT_NOT_APPROVED("AD1003", "승인되지 않은 계정입니다.", HttpStatus.FORBIDDEN.value()),
    INSUFFICIENT_PERMISSION("AD1004", "SUPER_ADMIN 권한이 필요합니다.", HttpStatus.FORBIDDEN.value()),
    REQUEST_ALREADY_PROCESSED("AD1005", "이미 처리된 신청입니다.", HttpStatus.CONFLICT.value()),
    EMAIL_ALREADY_REGISTERED("AD1006", "이미 등록된 이메일입니다.", HttpStatus.CONFLICT.value()),
    SIGNUP_REQUEST_EXPIRED("AD1007", "가입 신청이 만료되었습니다.", HttpStatus.GONE.value()),
    AUTH_PROCESSING_ERROR("AD1008", "관리자 인증 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value()),
    TOKEN_INVALID("AD1009", "관리자 인증 토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED.value()),
    SIGNUP_PENDING("AD1010", "아직 가입 신청이 승인되지 않았습니다. 관리자에게 문의하세요.", HttpStatus.FORBIDDEN.value()),
    SIGNUP_ALREADY_REQUESTED("AD1011", "이미 가입 신청된 계정입니다.", HttpStatus.CONFLICT.value()),
    REFRESH_TOKEN_INVALID("AD1012", "리프레시 토큰이 유효하지 않거나 만료되었습니다.", HttpStatus.UNAUTHORIZED.value());

    private final String code;
    private final String message;
    private final int status;

    AdminAuthErrorCode(String code, String message, int status) {
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
