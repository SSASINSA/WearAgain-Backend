package com.ssasinsa.wearagain.domain.ranking.dto;

import java.time.LocalDate;
import java.util.List;

public record RankingResponse(
        LocalDate comparedSnapshotDate,
        List<RankingEntry> topRanks,
        RankingEntry me
) {
    public static RankingResponse of(LocalDate comparedSnapshotDate, List<RankingEntry> topRanks, RankingEntry me) {
        return new RankingResponse(comparedSnapshotDate, topRanks, me);
    }
}
