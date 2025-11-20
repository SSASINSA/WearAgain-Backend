package com.ssasinsa.wearagain.domain.growth.service;

import com.ssasinsa.wearagain.domain.finance.repository.ImpactAnalyticsRepository;
import com.ssasinsa.wearagain.domain.growth.GrowthConstants;
import com.ssasinsa.wearagain.domain.growth.dto.ImpactSummary;
import com.ssasinsa.wearagain.domain.growth.dto.MascotStatusDto;
import com.ssasinsa.wearagain.domain.growth.entity.UserGrowth;
import com.ssasinsa.wearagain.domain.growth.exception.GrowthErrorCode;
import com.ssasinsa.wearagain.domain.growth.exception.GrowthException;
import com.ssasinsa.wearagain.domain.growth.repository.UserGrowthRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GrowthQueryService {

    private final UserGrowthRepository userGrowthRepository;
    private final ImpactAnalyticsRepository impactAnalyticsRepository;

    public MascotStatusDto getStatus(Long userId) {
        UserGrowth userGrowth = userGrowthRepository.findByUserId(userId)
                .orElseThrow(() -> new GrowthException(GrowthErrorCode.GROWTH_NOT_INITIALIZED));

        ImpactSummary impactSummary = impactAnalyticsRepository.aggregateByUserId(userId);
        if (impactSummary == null) {
            impactSummary = new ImpactSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        return MascotStatusDto.of(userGrowth, GrowthConstants.LEVEL_EXP_THRESHOLD, impactSummary);
    }
}
