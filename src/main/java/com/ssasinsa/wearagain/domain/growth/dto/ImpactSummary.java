package com.ssasinsa.wearagain.domain.growth.dto;

import java.math.BigDecimal;

public record ImpactSummary(
        BigDecimal co2Saved,
        BigDecimal waterSaved,
        BigDecimal energySaved
) {
}
