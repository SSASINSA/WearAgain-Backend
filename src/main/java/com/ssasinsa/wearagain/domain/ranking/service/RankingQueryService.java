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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
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

        List<RankingCandidate> topCandidates = rankingCandidateRepository.findCandidates(
                MagicScissorHistoryReason.USED_REPAIR,
                PageRequest.of(0, 10)
        );

        List<RankingEntry> topRanks = new ArrayList<>(topCandidates.size());
        for (int i = 0; i < topCandidates.size(); i++) {
            RankingCandidate candidate = topCandidates.get(i);
            int currentRank = i + 1;
            Integer previousRank = previousRankMap.get(candidate.userId());
            Integer rankChange = previousRank == null ? null : currentRank - previousRank;
            topRanks.add(RankingEntry.of(currentRank, candidate.nickname(), candidate.repairCount(), rankChange));
        }

        RankingEntry me = buildMeEntry(userId, previousRankMap);

        return RankingResponse.of(snapshotDate, topRanks, me);
    }

    private RankingEntry buildMeEntry(Long userId, Map<Long, Integer> previousRankMap) {
        Integer currentRank = rankingCandidateRepository.findRankForUser(
                MagicScissorHistoryReason.USED_REPAIR.name(),
                userId
        );
        if (currentRank == null) {
            return null;
        }
        Integer previousRank = previousRankMap.get(userId);
        Integer rankChange = previousRank == null ? null : currentRank - previousRank;

        RankingCandidate meCandidate = rankingCandidateRepository.findCandidateByUserId(
                        MagicScissorHistoryReason.USED_REPAIR,
                        userId
                )
                .orElse(null);
        if (meCandidate == null) {
            return null;
        }
        return RankingEntry.of(currentRank, meCandidate.nickname(), meCandidate.repairCount(), rankChange);
    }
}
