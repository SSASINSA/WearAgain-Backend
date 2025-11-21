package com.ssasinsa.wearagain.domain.growth.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.finance.entity.CreditHistory;
import com.ssasinsa.wearagain.domain.finance.repository.CreditHistoryRepository;
import com.ssasinsa.wearagain.domain.growth.GrowthConstants;
import com.ssasinsa.wearagain.domain.growth.dto.MagicScissorUseResult;
import com.ssasinsa.wearagain.domain.growth.entity.GrowthRewardRule;
import com.ssasinsa.wearagain.domain.growth.entity.MagicScissorHistory;
import com.ssasinsa.wearagain.domain.growth.entity.MagicScissorHistoryReason;
import com.ssasinsa.wearagain.domain.growth.entity.UserGrowth;
import com.ssasinsa.wearagain.domain.growth.exception.GrowthErrorCode;
import com.ssasinsa.wearagain.domain.growth.exception.GrowthException;
import com.ssasinsa.wearagain.domain.growth.repository.GrowthRewardRuleRepository;
import com.ssasinsa.wearagain.domain.growth.repository.MagicScissorHistoryRepository;
import com.ssasinsa.wearagain.domain.growth.repository.UserGrowthRepository;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GrowthCommandService {

    private static final int EXP_PER_USE = 35;
    private static final int MAX_LEVEL = 10;
    private static final int MAX_USE_PER_REQUEST = 20;
    private static final String CREDIT_REASON_GROWTH_REWARD = "GROWTH_LEVEL_REWARD";

    private final UserRepository userRepository;
    private final UserGrowthRepository userGrowthRepository;
    private final GrowthRewardRuleRepository growthRewardRuleRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final MagicScissorHistoryRepository magicScissorHistoryRepository;

    public MagicScissorUseResult useMagicScissors(Long userId, int useCount) {
        validateUseCount(useCount);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GrowthException(GrowthErrorCode.USER_NOT_FOUND));

        UserGrowth userGrowth = userGrowthRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new GrowthException(GrowthErrorCode.GROWTH_NOT_INITIALIZED));

        if (userGrowth.getMagicScissorCount() < useCount) {
            throw new GrowthException(GrowthErrorCode.INSUFFICIENT_MAGIC_SCISSORS);
        }

        userGrowth.useScissors(useCount);
        userGrowth.gainExperience(useCount * EXP_PER_USE);

        Map<Integer, Integer> rewardRuleMap = loadRewardRules();
        boolean rewardGranted = false;
        int totalRewardCredit = 0;

        while (userGrowth.getExp() >= GrowthConstants.LEVEL_EXP_THRESHOLD) {
            userGrowth.gainExperience(-GrowthConstants.LEVEL_EXP_THRESHOLD);
            if (userGrowth.getCurrentLevel() < MAX_LEVEL) {
                userGrowth.levelUp();
                int reward = grantRewardForLevel(userGrowth.getCurrentLevel(), user, rewardRuleMap);
                if (reward > 0) {
                    rewardGranted = true;
                    totalRewardCredit += reward;
                }
            } else {
                userGrowth.completeCycle();
                int reward = grantRewardForLevel(MAX_LEVEL, user, rewardRuleMap);
                if (reward > 0) {
                    rewardGranted = true;
                    totalRewardCredit += reward;
                }
                userGrowth.resetToLevelOneKeepingExp();
            }
        }

        saveHistory(user, userGrowth, null, -useCount, MagicScissorHistoryReason.USED_REPAIR, null);

        return new MagicScissorUseResult(
                userGrowth.getCurrentLevel(),
                userGrowth.getExp(),
                userGrowth.getMagicScissorCount(),
                userGrowth.getCycles(),
                rewardGranted,
                totalRewardCredit
        );
    }

    public void recordGrant(User user, UserGrowth userGrowth, Event event, int amount, String memo) {
        saveHistory(user, userGrowth, event, amount, MagicScissorHistoryReason.EARNED_EVENT, memo);
    }

    private int grantRewardForLevel(int levelRequired, User user, Map<Integer, Integer> rewardRuleMap) {
        int rewardCredit = getRewardCredit(levelRequired, rewardRuleMap);
        return applyReward(user, rewardCredit);
    }

    private void saveHistory(User user, UserGrowth userGrowth, Event event, int delta,
            MagicScissorHistoryReason reason, String memo) {
        MagicScissorHistory history = MagicScissorHistory.create(user, userGrowth, event, delta, reason, memo);
        magicScissorHistoryRepository.save(history);
    }

    private void validateUseCount(int useCount) {
        if (useCount < 1 || useCount > MAX_USE_PER_REQUEST) {
            throw new GrowthException(GrowthErrorCode.INVALID_MAGIC_SCISSOR_COUNT);
        }
    }

    private Map<Integer, Integer> loadRewardRules() {
        return growthRewardRuleRepository.findAll()
                .stream()
                .collect(Collectors.toMap(GrowthRewardRule::getLevelRequired, GrowthRewardRule::getCreditReward));
    }

    private int getRewardCredit(int level, Map<Integer, Integer> rewardRuleMap) {
        Integer credit = rewardRuleMap.get(level);
        if (credit == null) {
            throw new GrowthException(GrowthErrorCode.REWARD_RULE_NOT_FOUND);
        }
        return credit;
    }

    private int applyReward(User user, int rewardCredit) {
        if (rewardCredit <= 0) {
            return 0;
        }
        user.increaseCreditBalance(rewardCredit);
        CreditHistory history = CreditHistory.create(user, null, rewardCredit, CREDIT_REASON_GROWTH_REWARD);
        creditHistoryRepository.save(history);
        return rewardCredit;
    }
}
