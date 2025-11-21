package com.ssasinsa.wearagain.domain.ranking.entity;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import com.ssasinsa.wearagain.domain.auth.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "ranking_snapshots",
        uniqueConstraints = @UniqueConstraint(columnNames = {"snapshot_date", "users_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankingSnapshot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ranking_snapshots_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    @Column(name = "rank_value", nullable = false)
    private int rank;

    @Column(name = "repair_count", nullable = false)
    private int repairCount;

    @Column(name = "rank_change")
    private Integer rankChange;

    @Column(nullable = false, length = 255)
    private String nickname;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @Builder(access = AccessLevel.PRIVATE)
    private RankingSnapshot(
            LocalDate snapshotDate,
            int rank,
            int repairCount,
            Integer rankChange,
            String nickname,
            User user
    ) {
        this.snapshotDate = snapshotDate;
        this.rank = rank;
        this.repairCount = repairCount;
        this.rankChange = rankChange;
        this.nickname = nickname;
        this.user = user;
    }

    public static RankingSnapshot create(
            LocalDate snapshotDate,
            int rank,
            int repairCount,
            Integer rankChange,
            String nickname,
            User user
    ) {
        return RankingSnapshot.builder()
                .snapshotDate(snapshotDate)
                .rank(rank)
                .repairCount(repairCount)
                .rankChange(rankChange)
                .nickname(nickname)
                .user(user)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RankingSnapshot)) {
            return false;
        }
        RankingSnapshot other = (RankingSnapshot) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
