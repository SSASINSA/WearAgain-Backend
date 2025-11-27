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
import java.math.RoundingMode;
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

        return MascotStatusDto.of(userGrowth, GrowthConstants.LEVEL_EXP_THRESHOLD, scaleImpactForResponse(impactSummary));
    }

    public ImpactSummary getImpactSummary(Long userId) {
        ImpactSummary impactSummary = impactAnalyticsRepository.aggregateByUserId(userId);
        if (impactSummary == null) {
            return new ImpactSummary(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        return scaleImpactForResponse(impactSummary);
    }

    private ImpactSummary scaleImpactForResponse(ImpactSummary impactSummary) {
        return new ImpactSummary(
                scale(impactSummary.co2Saved()),
                scale(impactSummary.waterSaved()),
                scale(impactSummary.energySaved())
        );
    }

    private BigDecimal scale(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(3, RoundingMode.HALF_UP);
    }
}
