package com.ssasinsa.wearagain.domain.community.exception;

import com.ssasinsa.wearagain.global.exception.CustomException;

public class CommunityException extends CustomException {

    public CommunityException(CommunityErrorCode errorCode) {
        super(errorCode);
    }

    public CommunityException(CommunityErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}

