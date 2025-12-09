package com.ssasinsa.wearagain.domain.user.exception;

import com.ssasinsa.wearagain.global.exception.CustomException;

public class AdminManagementException extends CustomException {

    public AdminManagementException(AdminManagementErrorCode errorCode) {
        super(errorCode);
    }
}
