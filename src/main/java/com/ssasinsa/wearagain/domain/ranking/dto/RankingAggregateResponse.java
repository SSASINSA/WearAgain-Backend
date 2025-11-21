package com.ssasinsa.wearagain.domain.ranking.dto;

import java.time.LocalDate;

public record RankingAggregateResponse(
        LocalDate snapshotDate,
        String message
) {
    public static RankingAggregateResponse ok(LocalDate snapshotDate) {
        return new RankingAggregateResponse(snapshotDate, "랭킹 스냅샷 집계를 요청했습니다.");
    }
}
