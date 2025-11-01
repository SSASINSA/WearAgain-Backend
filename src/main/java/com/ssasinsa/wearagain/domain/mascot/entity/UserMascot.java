package com.ssasinsa.wearagain.domain.mascot.entity;

import com.ssasinsa.wearagain.auth.domain.User;
import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user_mascots")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserMascot extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_mascots_id", nullable = false, updatable = false)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false, unique = true)
    private User user;

    @Column(name = "current_level", nullable = false)
    private int currentLevel;

    @Column(nullable = false)
    private int exp;

    @Column(name = "repair_count", nullable = false)
    private int repairCount;

    @Column(nullable = false)
    private int cycles;

    @Column(name = "last_rewarded_at")
    private LocalDateTime lastRewardedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private UserMascot(User user, int currentLevel, int exp, int repairCount, int cycles, LocalDateTime lastRewardedAt) {
        this.user = user;
        this.currentLevel = currentLevel;
        this.exp = exp;
        this.repairCount = repairCount;
        this.cycles = cycles;
        this.lastRewardedAt = lastRewardedAt;
    }

    public static UserMascot create(User user) {
        return UserMascot.builder()
                .user(user)
                .currentLevel(1)
                .exp(0)
                .repairCount(0)
                .cycles(0)
                .build();
    }

    public void gainExperience(int amount) {
        this.exp += amount;
    }

    public void levelUp() {
        this.currentLevel += 1;
        this.exp = 0;
    }

    public void recordRepair() {
        this.repairCount += 1;
    }

    public void completeCycle() {
        this.cycles += 1;
    }

    public void updateLastRewardedAt(LocalDateTime rewardedAt) {
        this.lastRewardedAt = rewardedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserMascot)) {
            return false;
        }
        UserMascot other = (UserMascot) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
