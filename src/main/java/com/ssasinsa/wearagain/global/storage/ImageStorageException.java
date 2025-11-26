package com.ssasinsa.wearagain.global.storage;

import com.ssasinsa.wearagain.global.exception.CustomException;
import com.ssasinsa.wearagain.global.exception.ErrorCode;

public class ImageStorageException extends CustomException {

    public ImageStorageException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ImageStorageException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
