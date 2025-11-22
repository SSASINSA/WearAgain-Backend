package com.ssasinsa.wearagain.domain.ranking.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.ranking.entity.RankingSnapshot;
import com.ssasinsa.wearagain.domain.ranking.repository.RankingCandidate;
import com.ssasinsa.wearagain.domain.ranking.repository.RankingCandidateRepository;
import com.ssasinsa.wearagain.domain.ranking.repository.RankingSnapshotRepository;
import com.ssasinsa.wearagain.domain.ranking.repository.RankingSnapshotRepository.RankView;
import com.ssasinsa.wearagain.domain.growth.entity.MagicScissorHistoryReason;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingAggregationService {

    private static final int RETENTION_DAYS = 60;

    private final RankingCandidateRepository rankingCandidateRepository;
    private final RankingSnapshotRepository rankingSnapshotRepository;
    private final UserRepository userRepository;

    @Transactional
    public void aggregateDaily(LocalDate snapshotDate) {
        if (rankingSnapshotRepository.existsBySnapshotDate(snapshotDate)) {
            log.info("이미 생성된 랭킹 스냅샷이 있어 건너뜁니다. snapshotDate={}", snapshotDate);
            return;
        }

        List<RankingCandidate> candidates = rankingCandidateRepository.findCandidates(
                MagicScissorHistoryReason.USED_REPAIR,
                Pageable.unpaged()
        );
        if (candidates.isEmpty()) {
            log.info("랭킹 대상이 없어 스냅샷을 생성하지 않습니다. snapshotDate={}", snapshotDate);
            cleanUpSnapshots(snapshotDate);
            return;
        }

        Map<Long, User> userMap = loadUsers(candidates);
        Map<Long, Integer> yesterdayRankMap = loadRankMap(snapshotDate.minusDays(1));

        List<RankingSnapshot> snapshots = new ArrayList<>(candidates.size());
        int rank = 1;
        for (RankingCandidate candidate : candidates) {
            User user = userMap.get(candidate.userId());
            if (user == null) {
                log.warn("사용자 정보를 찾을 수 없어 랭킹 산출을 건너뜁니다. userId={}, snapshotDate={}",
                        candidate.userId(), snapshotDate);
                continue;
            }
            Integer rankChange = calculateRankChange(rank, candidate.userId(), yesterdayRankMap);
            RankingSnapshot snapshot = RankingSnapshot.create(
                    snapshotDate,
                    rank,
                    candidate.repairCount(),
                    rankChange,
                    candidate.nickname(),
                    user
            );

            snapshots.add(snapshot);
            rank++;
        }

        rankingSnapshotRepository.saveAll(snapshots);
        cleanUpSnapshots(snapshotDate);
        log.info("랭킹 스냅샷 생성 완료. snapshotDate={}, saved={}", snapshotDate, snapshots.size());
    }

    private Map<Long, User> loadUsers(List<RankingCandidate> candidates) {
        Set<Long> userIds = candidates.stream()
                .map(RankingCandidate::userId)
                .collect(Collectors.toSet());

        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
    }

    private Map<Long, Integer> loadRankMap(LocalDate snapshotDate) {
        List<RankView> previousRanks = rankingSnapshotRepository.findRankBySnapshotDate(snapshotDate);
        return previousRanks.stream()
                .collect(Collectors.toMap(RankView::getUserId, RankView::getRank));
    }

    private Integer calculateRankChange(int todayRank, Long userId, Map<Long, Integer> yesterdayRankMap) {
        Integer yesterdayRank = yesterdayRankMap.get(userId);
        if (yesterdayRank == null) {
            return null;
        }
        return todayRank - yesterdayRank;
    }

    private void cleanUpSnapshots(LocalDate snapshotDate) {
        LocalDate cutoff = snapshotDate.minusDays(RETENTION_DAYS);
        rankingSnapshotRepository.deleteBySnapshotDateBefore(cutoff);
    }
}
