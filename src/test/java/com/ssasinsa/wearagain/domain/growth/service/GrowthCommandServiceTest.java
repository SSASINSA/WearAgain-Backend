package com.ssasinsa.wearagain.domain.growth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.auth.entity.User;
import com.ssasinsa.wearagain.domain.auth.repository.UserRepository;
import com.ssasinsa.wearagain.domain.finance.entity.CreditHistory;
import com.ssasinsa.wearagain.domain.finance.repository.CreditHistoryRepository;
import com.ssasinsa.wearagain.domain.growth.dto.MagicScissorUseResult;
import com.ssasinsa.wearagain.domain.growth.entity.GrowthRewardRule;
import com.ssasinsa.wearagain.domain.growth.entity.UserGrowth;
import com.ssasinsa.wearagain.domain.growth.exception.GrowthErrorCode;
import com.ssasinsa.wearagain.domain.growth.exception.GrowthException;
import com.ssasinsa.wearagain.domain.growth.repository.GrowthRewardRuleRepository;
import com.ssasinsa.wearagain.domain.growth.repository.MagicScissorHistoryRepository;
import com.ssasinsa.wearagain.domain.growth.repository.UserGrowthRepository;
import java.util.List;
import java.util.Optional;
import org.mockito.Mockito;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GrowthCommandServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserGrowthRepository userGrowthRepository;
    @Mock
    private GrowthRewardRuleRepository growthRewardRuleRepository;
    @Mock
    private CreditHistoryRepository creditHistoryRepository;
    @Mock
    private MagicScissorHistoryRepository magicScissorHistoryRepository;

    private GrowthCommandService growthCommandService;

    @BeforeEach
    void setUp() {
        growthCommandService = new GrowthCommandService(
                userRepository,
                userGrowthRepository,
                growthRewardRuleRepository,
                creditHistoryRepository,
                magicScissorHistoryRepository
        );
    }

    @Test
    void should_use_scissors_and_gain_experience_without_reward() {
        User user = createUser(1L);
        UserGrowth userGrowth = UserGrowth.create(user);
        ReflectionTestUtils.setField(userGrowth, "magicScissorCount", 5);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userGrowthRepository.findByUserIdForUpdate(1L)).thenReturn(Optional.of(userGrowth));
        when(growthRewardRuleRepository.findAll()).thenReturn(List.of(rule(2, 100), rule(10, 300)));

        MagicScissorUseResult result = growthCommandService.useMagicScissors(1L, 2);

        assertThat(result.level()).isEqualTo(1);
        assertThat(result.exp()).isEqualTo(70);
        assertThat(result.magicScissorCount()).isEqualTo(3);
        assertThat(result.rewardGranted()).isFalse();
        verify(magicScissorHistoryRepository).save(any());
    }

    @Test
    void should_grant_reward_when_cycle_completed() {
        User user = createUser(2L);
        UserGrowth userGrowth = UserGrowth.create(user);
        ReflectionTestUtils.setField(userGrowth, "magicScissorCount", 10);
        ReflectionTestUtils.setField(userGrowth, "currentLevel", 10);
        ReflectionTestUtils.setField(userGrowth, "exp", 90);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userGrowthRepository.findByUserIdForUpdate(2L)).thenReturn(Optional.of(userGrowth));
        when(growthRewardRuleRepository.findAll()).thenReturn(List.of(rule(2, 100), rule(10, 300)));

        MagicScissorUseResult result = growthCommandService.useMagicScissors(2L, 5);

        assertThat(result.rewardGranted()).isTrue();
        assertThat(result.rewardCredit()).isEqualTo(400);
        assertThat(result.level()).isEqualTo(2);
        assertThat(result.exp()).isEqualTo(65);
        assertThat(result.cycles()).isEqualTo(1);
        assertThat(userGrowth.getMagicScissorCount()).isEqualTo(5);
        assertThat(user.getCreditBalance()).isEqualTo(400);
        verify(creditHistoryRepository, times(2)).save(any(CreditHistory.class));
    }

    @Test
    void should_retains_remaining_exp_when_multiple_levels_awarded() {
        User user = createUser(3L);
        UserGrowth userGrowth = UserGrowth.create(user);
        ReflectionTestUtils.setField(userGrowth, "magicScissorCount", 5);

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(userGrowthRepository.findByUserIdForUpdate(3L)).thenReturn(Optional.of(userGrowth));
        when(growthRewardRuleRepository.findAll()).thenReturn(List.of(rule(2, 100), rule(10, 300)));

        MagicScissorUseResult result = growthCommandService.useMagicScissors(3L, 3);

        assertThat(result.level()).isEqualTo(2);
        assertThat(result.exp()).isEqualTo(5);
        assertThat(result.magicScissorCount()).isEqualTo(2);
        assertThat(result.rewardGranted()).isTrue();
        assertThat(result.rewardCredit()).isEqualTo(100);
        verify(creditHistoryRepository).save(any(CreditHistory.class));
    }

    @Test
    void should_allow_multiple_use_when_below_max_level() {
        User user = createUser(4L);
        UserGrowth userGrowth = UserGrowth.create(user);
        ReflectionTestUtils.setField(userGrowth, "magicScissorCount", 10);
        ReflectionTestUtils.setField(userGrowth, "currentLevel", 9);
        ReflectionTestUtils.setField(userGrowth, "exp", 70);

        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(userGrowthRepository.findByUserIdForUpdate(4L)).thenReturn(Optional.of(userGrowth));
        when(growthRewardRuleRepository.findAll()).thenReturn(List.of(rule(2, 100), rule(10, 300)));

        MagicScissorUseResult result = growthCommandService.useMagicScissors(4L, 2);

        assertThat(result.level()).isEqualTo(10);
        assertThat(result.exp()).isEqualTo(40);
        assertThat(result.magicScissorCount()).isEqualTo(8);
        assertThat(result.rewardGranted()).isFalse();
        assertThat(result.rewardCredit()).isEqualTo(0);
        verify(creditHistoryRepository, never()).save(any(CreditHistory.class));
    }

    @Test
    void should_throw_when_scissor_count_invalid() {
        assertThatThrownBy(() -> growthCommandService.useMagicScissors(1L, 0))
                .isInstanceOf(GrowthException.class)
                .extracting("errorCode")
                .isEqualTo(GrowthErrorCode.INVALID_MAGIC_SCISSOR_COUNT);
    }

    @Test
    void should_throw_when_reward_rule_missing() {
        User user = createUser(5L);
        UserGrowth userGrowth = UserGrowth.create(user);
        ReflectionTestUtils.setField(userGrowth, "magicScissorCount", 5);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userGrowthRepository.findByUserIdForUpdate(5L)).thenReturn(Optional.of(userGrowth));
        when(growthRewardRuleRepository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> growthCommandService.useMagicScissors(5L, 3))
                .isInstanceOf(GrowthException.class)
                .extracting("errorCode")
                .isEqualTo(GrowthErrorCode.REWARD_RULE_NOT_FOUND);
        verify(creditHistoryRepository, never()).save(any());
    }

    private User createUser(Long id) {
        User user = User.create("user@example.com", "사용자", null);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private GrowthRewardRule rule(int level, int credit) {
        return GrowthRewardRule.create(level, credit);
    }
}
