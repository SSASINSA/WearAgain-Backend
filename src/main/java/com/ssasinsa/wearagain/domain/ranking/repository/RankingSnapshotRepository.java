package com.ssasinsa.wearagain.domain.ranking.repository;

import com.ssasinsa.wearagain.domain.ranking.entity.RankingSnapshot;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RankingSnapshotRepository extends JpaRepository<RankingSnapshot, Long> {

    boolean existsBySnapshotDate(LocalDate snapshotDate);

    List<RankingSnapshot> findBySnapshotDate(LocalDate snapshotDate);

    List<RankingSnapshot> findTop10BySnapshotDateOrderByRankAsc(LocalDate snapshotDate);

    Optional<RankingSnapshot> findBySnapshotDateAndUserId(LocalDate snapshotDate, Long userId);

    @Query("select rs.user.id as userId, rs.rank as rank from RankingSnapshot rs where rs.snapshotDate = :snapshotDate")
    List<RankView> findRankBySnapshotDate(@Param("snapshotDate") LocalDate snapshotDate);

    void deleteBySnapshotDateBefore(LocalDate snapshotDate);

    @Query("select max(rs.snapshotDate) from RankingSnapshot rs")
    Optional<LocalDate> findLatestSnapshotDate();

    interface RankView {
        Long getUserId();

        Integer getRank();
    }
}
