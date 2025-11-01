package com.ssasinsa.wearagain.auth.exception;
import com.ssasinsa.wearagain.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum AuthErrorCode implements ErrorCode {
    AUTHORIZATION_CODE_REQUIRED("A1004", "인가 코드가 필요합니다.", HttpStatus.BAD_REQUEST.value()),
    GOOGLE_TOKEN_REQUEST_FAILED("A1001", "Google 토큰 발급에 실패했습니다.", HttpStatus.UNAUTHORIZED.value()),
    GOOGLE_USERINFO_REQUEST_FAILED("A1001", "Google 사용자 정보를 불러오지 못했습니다.", HttpStatus.UNAUTHORIZED.value()),
    KAKAO_TOKEN_REQUEST_FAILED("A1002", "Kakao 토큰 발급에 실패했습니다.", HttpStatus.UNAUTHORIZED.value()),
    KAKAO_USERINFO_REQUEST_FAILED("A1002", "Kakao 사용자 정보를 불러오지 못했습니다.", HttpStatus.UNAUTHORIZED.value()),
    KAKAO_EMAIL_NOT_PROVIDED("A1002", "Kakao 계정에서 이메일을 제공하지 않았습니다.", HttpStatus.UNAUTHORIZED.value()),
    APPLE_TOKEN_REQUEST_FAILED("A1003", "Apple 토큰 발급에 실패했습니다.", HttpStatus.UNAUTHORIZED.value()),
    APPLE_USERINFO_REQUEST_FAILED("A1003", "Apple 사용자 정보를 불러오지 못했습니다.", HttpStatus.UNAUTHORIZED.value()),
    REFRESH_TOKEN_INVALID("A1005", "Refresh Token 만료 또는 불일치.", HttpStatus.UNAUTHORIZED.value()),
    ACCESS_TOKEN_EXPIRED("A1006", "Access Token이 만료되었습니다.", HttpStatus.UNAUTHORIZED.value()),
    REFRESH_TOKEN_REUSED("A1007", "재사용된 Refresh Token 입니다.", HttpStatus.UNAUTHORIZED.value());

    private final String code;
    private final String message;
    private final int status;

    AuthErrorCode(String code, String message, int status) {
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
