package com.ssasinsa.wearagain.domain.user.dto.admin;

import java.math.BigDecimal;

public record AdminImpactSummaryResponse(
        BigDecimal co2Saved,
        BigDecimal waterSaved,
        BigDecimal energySaved
) {
    public static AdminImpactSummaryResponse zero() {
        BigDecimal zero = BigDecimal.ZERO.setScale(2);
        return new AdminImpactSummaryResponse(zero, zero, zero);
    }
}
