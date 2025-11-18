package com.ssasinsa.wearagain.domain.growth.entity;

import com.ssasinsa.wearagain.common.entity.BaseTimeEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "growth_reward_rules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AttributeOverrides({
        @AttributeOverride(name = "createdAt", column = @Column(name = "created_at", updatable = false)),
        @AttributeOverride(name = "updatedAt", column = @Column(name = "created_at", insertable = false, updatable = false))
})
public class GrowthRewardRule extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "growth_reward_rules_id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "level_required", nullable = false, unique = true)
    private int levelRequired;

    @Column(name = "credit_reward", nullable = false)
    private int creditReward;

    @Builder(access = AccessLevel.PRIVATE)
    private GrowthRewardRule(int levelRequired, int creditReward) {
        this.levelRequired = levelRequired;
        this.creditReward = creditReward;
    }

    public static GrowthRewardRule create(int levelRequired, int creditReward) {
        return GrowthRewardRule.builder()
                .levelRequired(levelRequired)
                .creditReward(creditReward)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GrowthRewardRule)) {
            return false;
        }
        GrowthRewardRule other = (GrowthRewardRule) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
