package com.ssasinsa.wearagain.domain.growth.entity;

import com.ssasinsa.wearagain.domain.auth.entity.User;
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
@Table(name = "user_growths")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserGrowth extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_growths_id", nullable = false, updatable = false)
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

    @Column(name = "magic_scissor_count", nullable = false)
    private int magicScissorCount;

    @Column(name = "total_scissor_used", nullable = false)
    private int totalScissorUsed;

    @Column(name = "last_rewarded_at")
    private LocalDateTime lastRewardedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private UserGrowth(
            User user,
            int currentLevel,
            int exp,
            int repairCount,
            int cycles,
            int magicScissorCount,
            int totalScissorUsed,
            LocalDateTime lastRewardedAt
    ) {
        this.user = user;
        this.currentLevel = currentLevel;
        this.exp = exp;
        this.repairCount = repairCount;
        this.cycles = cycles;
        this.magicScissorCount = magicScissorCount;
        this.totalScissorUsed = totalScissorUsed;
        this.lastRewardedAt = lastRewardedAt;
    }

    public static UserGrowth create(User user) {
        return UserGrowth.builder()
                .user(user)
                .currentLevel(1)
                .exp(0)
                .repairCount(0)
                .cycles(0)
                .magicScissorCount(0)
                .totalScissorUsed(0)
                .build();
    }

    public void addScissors(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("scissor amount must be positive");
        }
        this.magicScissorCount += amount;
    }

    public void useScissors(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("scissor amount must be positive");
        }
        if (this.magicScissorCount < amount) {
            throw new IllegalStateException("insufficient magic scissors");
        }
        this.magicScissorCount -= amount;
        this.totalScissorUsed += amount;
    }

    public void gainExperience(int amount) {
        this.exp += amount;
    }

    public void levelUp() {
        this.currentLevel += 1;
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

    public void resetLevel() {
        this.currentLevel = 1;
        this.exp = 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserGrowth)) {
            return false;
        }
        UserGrowth other = (UserGrowth) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
