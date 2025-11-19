package com.ssasinsa.wearagain.domain.growth.exception;

import com.ssasinsa.wearagain.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum GrowthErrorCode implements ErrorCode {
    GROWTH_NOT_INITIALIZED("G7001", "성장 정보가 초기화되지 않았습니다.", HttpStatus.NOT_FOUND.value()),
    INVALID_MAGIC_SCISSOR_COUNT("G7002", "가위 사용 수량이 허용 범위를 벗어났습니다.", HttpStatus.BAD_REQUEST.value()),
    INSUFFICIENT_MAGIC_SCISSORS("G7003", "보유 중인 마법의 가위가 부족합니다.", HttpStatus.BAD_REQUEST.value()),
    USER_NOT_FOUND("G7004", "사용자 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()),
    REWARD_RULE_NOT_FOUND("G7005", "성장 보상 규칙이 정의되어 있지 않습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());

    private final String code;
    private final String message;
    private final int status;

    GrowthErrorCode(String code, String message, int status) {
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
