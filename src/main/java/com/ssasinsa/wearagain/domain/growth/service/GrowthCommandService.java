package com.ssasinsa.wearagain.domain.growth.service;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.event.entity.Event;
import com.ssasinsa.wearagain.domain.finance.entity.CreditHistory;
import com.ssasinsa.wearagain.domain.finance.repository.CreditHistoryRepository;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GrowthCommandService {

    private static final int EXP_PER_USE = 35;
    private static final int LEVEL_EXP_THRESHOLD = 100;
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

        int actualUseCount = adjustUseCountForReward(userGrowth, useCount);

        userGrowth.useScissors(actualUseCount);
        userGrowth.gainExperience(actualUseCount * EXP_PER_USE);

        boolean rewardGranted = false;
        int totalRewardCredit = 0;

        while (userGrowth.getExp() >= LEVEL_EXP_THRESHOLD) {
            userGrowth.gainExperience(-LEVEL_EXP_THRESHOLD);
            if (userGrowth.getCurrentLevel() < MAX_LEVEL) {
                userGrowth.levelUp();
            } else {
                userGrowth.completeCycle();
                int reward = grantCycleReward(user);
                if (reward > 0) {
                    rewardGranted = true;
                    totalRewardCredit += reward;
                }
            }
        }

        saveHistory(user, userGrowth, null, -actualUseCount, MagicScissorHistoryReason.USED_REPAIR, null);

        return new MagicScissorUseResult(
                userGrowth.getCurrentLevel(),
                userGrowth.getExp(),
                userGrowth.getMagicScissorCount(),
                userGrowth.getCycles(),
                rewardGranted,
                totalRewardCredit
        );
    }

    private int adjustUseCountForReward(UserGrowth userGrowth, int requestedUseCount) {
        if (userGrowth.getCurrentLevel() < MAX_LEVEL) {
            return requestedUseCount;
        }

        int remainingExp = LEVEL_EXP_THRESHOLD - userGrowth.getExp();
        if (remainingExp <= 0) {
            remainingExp = LEVEL_EXP_THRESHOLD;
        }
        int usesNeeded = (int) Math.ceil((double) remainingExp / EXP_PER_USE);
        usesNeeded = Math.max(usesNeeded, 1);
        return Math.min(requestedUseCount, usesNeeded);
    }

    public void recordGrant(User user, UserGrowth userGrowth, Event event, int amount, String memo) {
        saveHistory(user, userGrowth, event, amount, MagicScissorHistoryReason.EARNED_EVENT, memo);
    }

    private int grantCycleReward(User user) {
        GrowthRewardRule rewardRule = growthRewardRuleRepository.findByLevelRequired(MAX_LEVEL)
                .orElseThrow(() -> new GrowthException(GrowthErrorCode.REWARD_RULE_NOT_FOUND));

        int rewardCredit = rewardRule.getCreditReward();
        if (rewardCredit <= 0) {
            return 0;
        }

        user.increaseCreditBalance(rewardCredit);
        CreditHistory history = CreditHistory.create(user, null, rewardCredit, CREDIT_REASON_GROWTH_REWARD);
        creditHistoryRepository.save(history);
        return rewardCredit;
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
}
