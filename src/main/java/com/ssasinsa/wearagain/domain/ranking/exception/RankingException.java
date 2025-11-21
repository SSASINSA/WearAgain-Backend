package com.ssasinsa.wearagain.domain.ranking.exception;

import com.ssasinsa.wearagain.global.exception.CustomException;

public class RankingException extends CustomException {
    public RankingException(RankingErrorCode errorCode) {
        super(errorCode);
    }
}
