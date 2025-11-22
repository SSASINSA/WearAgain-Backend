package com.ssasinsa.wearagain.domain.ranking.dto;

public record RankingEntry(
        int rank,
        String nickname,
        int repairCount,
        Integer rankChange
) {
    public static RankingEntry of(int rank, String nickname, int repairCount, Integer rankChange) {
        return new RankingEntry(rank, nickname, repairCount, rankChange);
    }
}
