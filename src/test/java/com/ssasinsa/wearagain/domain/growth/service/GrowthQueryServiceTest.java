package com.ssasinsa.wearagain.domain.growth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.ssasinsa.wearagain.domain.finance.repository.ImpactAnalyticsRepository;
import com.ssasinsa.wearagain.domain.growth.dto.ImpactSummary;
import com.ssasinsa.wearagain.domain.growth.dto.MascotStatusDto;
import com.ssasinsa.wearagain.domain.growth.entity.UserGrowth;
import com.ssasinsa.wearagain.domain.growth.exception.GrowthErrorCode;
import com.ssasinsa.wearagain.domain.growth.exception.GrowthException;
import com.ssasinsa.wearagain.domain.growth.repository.UserGrowthRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GrowthQueryServiceTest {

    @Mock
    private UserGrowthRepository userGrowthRepository;
    @Mock
    private ImpactAnalyticsRepository impactAnalyticsRepository;

    private GrowthQueryService growthQueryService;

    @BeforeEach
    void setUp() {
        growthQueryService = new GrowthQueryService(userGrowthRepository, impactAnalyticsRepository);
    }

    @Test
    void should_return_status_with_impact() {
        UserGrowth userGrowth = UserGrowth.create(createUser());
        ReflectionTestUtils.setField(userGrowth, "currentLevel", 3);
        ReflectionTestUtils.setField(userGrowth, "exp", 40);
        ReflectionTestUtils.setField(userGrowth, "magicScissorCount", 5);
        ReflectionTestUtils.setField(userGrowth, "cycles", 1);

        when(userGrowthRepository.findByUserId(1L)).thenReturn(Optional.of(userGrowth));
        when(impactAnalyticsRepository.aggregateByUserId(1L)).thenReturn(new ImpactSummary(
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                new BigDecimal("30.00")
        ));

        MascotStatusDto dto = growthQueryService.getStatus(1L);

        assertThat(dto.level()).isEqualTo(3);
        assertThat(dto.exp()).isEqualTo(40);
        assertThat(dto.impact().co2Saved()).isEqualTo(new BigDecimal("10.00"));
    }

    @Test
    void should_throw_when_growth_not_initialized() {
        when(userGrowthRepository.findByUserId(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> growthQueryService.getStatus(1L))
                .isInstanceOf(GrowthException.class)
                .extracting("errorCode")
                .isEqualTo(GrowthErrorCode.GROWTH_NOT_INITIALIZED);
    }

    private com.ssasinsa.wearagain.domain.auth.entity.User createUser() {
        com.ssasinsa.wearagain.domain.auth.entity.User user = com.ssasinsa.wearagain.domain.auth.entity.User.create(
                "user@example.com",
                "사용자",
                null
        );
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }
}
