package com.ssasinsa.wearagain.domain.ranking.service;

import com.ssasinsa.wearagain.domain.growth.entity.MagicScissorHistoryReason;
import com.ssasinsa.wearagain.domain.ranking.dto.RankingEntry;
import com.ssasinsa.wearagain.domain.ranking.dto.RankingResponse;
import com.ssasinsa.wearagain.domain.ranking.entity.RankingSnapshot;
import com.ssasinsa.wearagain.domain.ranking.exception.RankingErrorCode;
import com.ssasinsa.wearagain.domain.ranking.exception.RankingException;
import com.ssasinsa.wearagain.domain.ranking.repository.RankingCandidate;
import com.ssasinsa.wearagain.domain.ranking.repository.RankingCandidateRepository;
import com.ssasinsa.wearagain.domain.ranking.repository.RankingSnapshotRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private final RankingCandidateRepository rankingCandidateRepository;
    private final RankingSnapshotRepository rankingSnapshotRepository;

    public RankingResponse getLatestRanking(Long userId) {
        LocalDate snapshotDate = rankingSnapshotRepository.findLatestSnapshotDate()
                .orElseThrow(() -> new RankingException(RankingErrorCode.RANKING_SNAPSHOT_NOT_READY));

        Map<Long, Integer> previousRankMap = rankingSnapshotRepository.findRankBySnapshotDate(snapshotDate)
                .stream()
                .collect(Collectors.toMap(RankingSnapshotRepository.RankView::getUserId, RankingSnapshotRepository.RankView::getRank));

        List<RankingCandidate> candidates = rankingCandidateRepository.findCandidates(MagicScissorHistoryReason.USED_REPAIR);

        List<RankingEntry> topRanks = new ArrayList<>();
        RankingEntry me = null;
        int rank = 1;

        for (RankingCandidate candidate : candidates) {
            Integer previousRank = previousRankMap.get(candidate.userId());
            Integer rankChange = previousRank == null ? null : rank - previousRank;
            RankingEntry entry = RankingEntry.of(
                    rank,
                    candidate.nickname(),
                    candidate.repairCount(),
                    rankChange
            );

            if (rank <= 10) {
                topRanks.add(entry);
            }
            if (me == null && candidate.userId().equals(userId)) {
                me = entry;
            }
            rank++;
        }

        return RankingResponse.of(snapshotDate, topRanks, me);
    }
}
