package com.ssasinsa.wearagain.domain.ranking.dto;

import java.time.LocalDate;
import java.util.List;

public record RankingResponse(
        LocalDate snapshotDate,
        List<RankingEntry> topRanks,
        RankingEntry me
) {
    public static RankingResponse of(LocalDate snapshotDate, List<RankingEntry> topRanks, RankingEntry me) {
        return new RankingResponse(snapshotDate, topRanks, me);
    }
}
