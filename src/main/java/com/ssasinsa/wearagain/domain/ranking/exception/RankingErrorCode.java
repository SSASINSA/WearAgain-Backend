package com.ssasinsa.wearagain.domain.ranking.exception;

import com.ssasinsa.wearagain.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RankingErrorCode implements ErrorCode {
    RANKING_SNAPSHOT_NOT_READY("R1001", "랭킹 스냅샷이 아직 생성되지 않았습니다.", 503);

    private final String code;
    private final String message;
    private final int status;
}
