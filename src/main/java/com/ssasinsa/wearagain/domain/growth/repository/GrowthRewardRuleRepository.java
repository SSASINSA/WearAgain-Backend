package com.ssasinsa.wearagain.domain.growth.repository;

import com.ssasinsa.wearagain.domain.growth.entity.GrowthRewardRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrowthRewardRuleRepository extends JpaRepository<GrowthRewardRule, Long> {

    Optional<GrowthRewardRule> findByLevelRequired(int levelRequired);
}
