package com.ssasinsa.wearagain.domain.ranking.repository;

import java.time.LocalDateTime;

public record RankingCandidate(
        Long userId,
        String nickname,
        int repairCount,
        LocalDateTime lastUsedAt
) {
}
