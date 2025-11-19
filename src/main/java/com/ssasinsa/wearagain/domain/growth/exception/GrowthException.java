package com.ssasinsa.wearagain.domain.growth.exception;

import com.ssasinsa.wearagain.global.exception.CustomException;

public class GrowthException extends CustomException {

    public GrowthException(GrowthErrorCode errorCode) {
        super(errorCode);
    }
}
