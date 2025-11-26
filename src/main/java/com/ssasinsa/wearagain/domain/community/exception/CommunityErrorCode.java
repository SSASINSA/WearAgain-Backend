package com.ssasinsa.wearagain.domain.community.exception;

import com.ssasinsa.wearagain.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CommunityErrorCode implements ErrorCode {
    POST_NOT_FOUND("CM1001", "게시글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()),
    POST_UPDATE_FORBIDDEN("CM1002", "게시글 수정 권한이 없습니다.", HttpStatus.FORBIDDEN.value()),
    POST_DELETE_FORBIDDEN("CM1003", "게시글 삭제 권한이 없습니다.", HttpStatus.FORBIDDEN.value()),
    CATEGORY_NOT_FOUND("CM1004", "카테고리를 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()),
    INVALID_POST_DATA("CM1005", "게시글 데이터가 올바르지 않습니다.", HttpStatus.BAD_REQUEST.value()),
    REPORT_ALREADY_EXISTS("CM1006", "이미 신고한 게시글입니다.", HttpStatus.BAD_REQUEST.value());

    private final String code;
    private final String message;
    private final int status;

    CommunityErrorCode(String code, String message, int status) {
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

