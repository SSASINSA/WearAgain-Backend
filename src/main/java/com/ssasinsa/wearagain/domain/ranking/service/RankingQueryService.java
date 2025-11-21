package com.ssasinsa.wearagain.domain.ranking.service;

import com.ssasinsa.wearagain.domain.ranking.dto.RankingEntry;
import com.ssasinsa.wearagain.domain.ranking.dto.RankingResponse;
import com.ssasinsa.wearagain.domain.ranking.entity.RankingSnapshot;
import com.ssasinsa.wearagain.domain.ranking.exception.RankingErrorCode;
import com.ssasinsa.wearagain.domain.ranking.exception.RankingException;
import com.ssasinsa.wearagain.domain.ranking.repository.RankingSnapshotRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankingQueryService {

    private static final ZoneId ZONE_SEOUL = ZoneId.of("Asia/Seoul");

    private final RankingSnapshotRepository rankingSnapshotRepository;

    public RankingResponse getLatestRanking(Long userId) {
        LocalDate targetDate = LocalDate.now(ZONE_SEOUL).minusDays(1);
        boolean exists = rankingSnapshotRepository.existsBySnapshotDate(targetDate);
        if (!exists) {
            throw new RankingException(RankingErrorCode.RANKING_SNAPSHOT_NOT_READY);
        }

        List<RankingEntry> topRanks = rankingSnapshotRepository.findTop10BySnapshotDateOrderByRankAsc(targetDate)
                .stream()
                .map(this::toEntry)
                .collect(Collectors.toList());

        RankingEntry me = rankingSnapshotRepository.findBySnapshotDateAndUserId(targetDate, userId)
                .map(this::toEntry)
                .orElse(null);

        return RankingResponse.of(targetDate, topRanks, me);
    }

    private RankingEntry toEntry(RankingSnapshot snapshot) {
        return RankingEntry.of(
                snapshot.getRank(),
                snapshot.getNickname(),
                snapshot.getRepairCount(),
                snapshot.getRankChange()
        );
    }
}
